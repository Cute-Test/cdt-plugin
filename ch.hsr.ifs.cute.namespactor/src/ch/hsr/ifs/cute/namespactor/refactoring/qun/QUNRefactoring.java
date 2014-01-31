/******************************************************************************
 * Copyright (c) 2012 Institute for Software, HSR Hochschule fuer Technik 
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 *
 * Contributors:
 * 	Ueli Kunz <kunz@ideadapt.net>, Jules Weder <julesweder@gmail.com> - initial API and implementation
 ******************************************************************************/
package ch.hsr.ifs.cute.namespactor.refactoring.qun;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.cute.namespactor.astutil.ASTNodeFactory;
import ch.hsr.ifs.cute.namespactor.astutil.NSNameHelper;
import ch.hsr.ifs.cute.namespactor.astutil.NSNodeHelper;
import ch.hsr.ifs.cute.namespactor.astutil.NSSelectionHelper;
import ch.hsr.ifs.cute.namespactor.refactoring.TemplateIdFactory;
import ch.hsr.ifs.cute.namespactor.refactoring.iu.InlineRefactoringBase;
import ch.hsr.ifs.cute.namespactor.refactoring.iu.InlineRefactoringContext;
import ch.hsr.ifs.cute.namespactor.resources.Labels;

/**
 * @author kunz@ideadapt.net
 * */
@SuppressWarnings("restriction")
public class QUNRefactoring extends InlineRefactoringBase {

	public QUNRefactoring(ICElement element, ISelection selection, ICProject project) {
		super(element, selection, project);
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);

		super.checkInitialConditions(sm.newChild(6));

		if (initStatus.hasFatalError()) {
			sm.done();
			return initStatus;
		}

		IASTName selectedName = NSSelectionHelper.getSelectedName(selectedRegion, getAST(tu, pm));

		if (selectedName == null) {
			initStatus.addFatalError(Labels.QUN_NoNameSelected);
		} else if (selectedName.getParent() instanceof ICPPASTDeclarator) {
			initStatus.addFatalError(Labels.QUN_DeclaratorNameSelected);
		} else if (selectedName.getParent() instanceof ICPPASTQualifiedName || selectedName instanceof ICPPASTQualifiedName) {
			initStatus.addFatalError(Labels.QUN_SelectedNameAlreadyQualified);
		} else {

			IBinding selectedNameBinding = selectedName.resolveBinding();
			if (selectedNameBinding instanceof ICPPSpecialization) {
				selectedNameBinding = ((ICPPSpecialization) selectedNameBinding).getSpecializedBinding();
			}
			addReplacement(selectedName, ASTNodeFactory.getDefault().newQualifiedNameNode(NSNameHelper.getQualifiedName(selectedNameBinding)));

		}

		sm.done();
		return initStatus;
	}

	@Override
	protected TemplateIdFactory getTemplateIdFactory(ICPPASTTemplateId templateId, InlineRefactoringContext ctx) {
		return new QUNTemplateIdFactory(templateId, ctx);
	}

	@Override
	protected void processTemplateVariableDeclaration(IASTName childRefNode, InlineRefactoringContext ctx) {
		IASTName nodeToReplace = null;
		ICPPASTTemplateId vTemplId = (ICPPASTTemplateId) childRefNode.getParent();

		if (ctx.templateIdsToIgnore.contains(vTemplId)) {
			return;
		}

		ICPPASTTemplateId outerMostTemplateId = NSNodeHelper.findOuterMost(ICPPASTTemplateId.class, vTemplId);
		if (outerMostTemplateId == null) {
			outerMostTemplateId = vTemplId;
		}

		nodeToReplace = outerMostTemplateId;
		if (outerMostTemplateId.getParent() instanceof ICPPASTQualifiedName) {
			nodeToReplace = (IASTName) outerMostTemplateId.getParent();
		}

		IASTName newTemplId = getTemplateIdFactory(outerMostTemplateId, ctx).buildTemplate();

		addReplacement(nodeToReplace, newTemplId);
	}

}