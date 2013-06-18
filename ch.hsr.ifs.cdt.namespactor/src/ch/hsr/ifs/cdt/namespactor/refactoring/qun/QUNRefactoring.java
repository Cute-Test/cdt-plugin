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
package ch.hsr.ifs.cdt.namespactor.refactoring.qun;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.cdt.namespactor.astutil.ASTNodeFactory;
import ch.hsr.ifs.cdt.namespactor.astutil.NSNameHelper;
import ch.hsr.ifs.cdt.namespactor.astutil.NSNodeHelper;
import ch.hsr.ifs.cdt.namespactor.astutil.NSSelectionHelper;
import ch.hsr.ifs.cdt.namespactor.refactoring.NodeDefinitionNotInWorkspaceException;
import ch.hsr.ifs.cdt.namespactor.refactoring.TemplateIdFactory;
import ch.hsr.ifs.cdt.namespactor.refactoring.iu.InlineRefactoringBase;
import ch.hsr.ifs.cdt.namespactor.refactoring.iu.InlineRefactoringContext;
import ch.hsr.ifs.cdt.namespactor.refactoring.iu.InlineTemplateIdFactory;
import ch.hsr.ifs.cdt.namespactor.refactoring.iu.NamespaceInlineContext;
import ch.hsr.ifs.cdt.namespactor.resources.Labels;

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

		if(selectedName == null){
			initStatus.addFatalError(Labels.QUN_NoNameSelected);
		}else if(selectedName.getParent() instanceof ICPPASTDeclarator){
			initStatus.addFatalError(Labels.QUN_DeclaratorNameSelected);
		}else if(selectedName.getParent() instanceof ICPPASTQualifiedName || selectedName instanceof ICPPASTQualifiedName){
			initStatus.addFatalError(Labels.QUN_SelectedNameAlreadyQualified);
		}else{
			
			IBinding selectedNameBinding = selectedName.resolveBinding();
			try {
				if(isPartOfTemplateVariableDeclaration(selectedName)){
					initContext(selectedName);
					processTemplateVariableDeclaration(selectedName, ctx);

				} else if(selectedNameBinding instanceof ICPPSpecialization){

					selectedNameBinding = ((ICPPSpecialization) selectedNameBinding).getSpecializedBinding();
				}

				addReplacement(selectedName, new ASTNodeFactory().newQualifiedNameNode(CPPVisitor.getQualifiedName(selectedNameBinding)));
			} catch (NodeDefinitionNotInWorkspaceException e) {
				initStatus.addFatalError(Labels.IU_SysNode);
				e.printStackTrace();
			}
		}

		sm.done();
		return initStatus;
	}

	private void initContext(IASTName selectedName) throws CoreException, NodeDefinitionNotInWorkspaceException {
		ctx.selectedName        = selectedName;
		ctx.templateIdsToIgnore = new HashSet<ICPPASTTemplateId>();
		IBinding selectedNameBinding = ctx.selectedName.resolveBinding();
		
		List<IBinding> childrenBindings = new ArrayList<IBinding>();
		childrenBindings.add(selectedNameBinding);

		IIndexName[] declNames = getIndex().findDeclarations(selectedNameBinding);
		ICPPASTNamespaceDefinition nsDefNode = NSNodeHelper.findAncestorOf(getNodeOf(declNames[0], npm), ICPPASTNamespaceDefinition.class);// AOB
																																			// Exc....
		ICPPASTQualifiedName newQName        = ASTNodeFactory.getDefault().newQualifiedNameNode(CPPVisitor.getQualifiedName(selectedNameBinding));
		
		ctx.enclosingNSContext = new NamespaceInlineContext();
		ctx.enclosingNSContext.namespaceDefNode  = nsDefNode;
		ctx.enclosingNSContext.usingName         = NSNameHelper.copyQualifers(newQName);
	}
	
	@Override
	protected TemplateIdFactory getTemplateIdFactory(ICPPASTTemplateId templateId, InlineRefactoringContext ctx) {
		return new InlineTemplateIdFactory(templateId, ctx);
	}	
}