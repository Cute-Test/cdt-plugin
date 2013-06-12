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
package ch.hsr.ifs.cdt.namespactor.refactoring.iu;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelection;

import ch.hsr.ifs.cdt.namespactor.NamespactorActivator;
import ch.hsr.ifs.cdt.namespactor.astutil.NSNodeHelper;
import ch.hsr.ifs.cdt.namespactor.refactoring.RefactoringBase;
import ch.hsr.ifs.cdt.namespactor.refactoring.TemplateIdFactory;
import ch.hsr.ifs.cdt.namespactor.refactoring.rewrite.ASTRewriteStore;

/**
 * @author kunz@ideadapt.net
 * */
public abstract class InlineRefactoringBase extends RefactoringBase {
	
	protected Map<IASTName, IASTName> nodesToReplace = new HashMap<IASTName, IASTName>();
	protected InlineRefactoringContext ctx = new InlineRefactoringContext();
	protected NullProgressMonitor npm = new NullProgressMonitor();

	public InlineRefactoringBase(ICElement element, ISelection selection, ICProject project) {
		super(element, selection, project);
	}
	
	protected abstract TemplateIdFactory getTemplateIdFactory(ICPPASTTemplateId templateId, InlineRefactoringContext ctx);

	protected void processTemplateVariableDeclaration(IASTName childRefNode, InlineRefactoringContext ctx) {
		IASTName nodeToReplace = null;
		ICPPASTTemplateId vTemplId = (ICPPASTTemplateId) childRefNode.getParent();
		
		if(ctx.templateIdsToIgnore.contains(vTemplId)){
			return;
		}
		
		ICPPASTTemplateId outerMostTemplateId = NSNodeHelper.findOuterMost(ICPPASTTemplateId.class, vTemplId);
		if(outerMostTemplateId == null){
			outerMostTemplateId = vTemplId;
		}
		
		nodeToReplace = outerMostTemplateId;
		if(outerMostTemplateId.getParent() instanceof ICPPASTQualifiedName){
			nodeToReplace = (IASTName) outerMostTemplateId.getParent();
		}
		
		IASTName newTemplId = getTemplateIdFactory(outerMostTemplateId, ctx).buildTemplate();

		addReplacement(nodeToReplace, newTemplId);
	}

	protected void addReplacement(IASTName nodeToReplace, IASTName newNameNode) {
		if(nodeToReplace != null){
			nodesToReplace.put(nodeToReplace, newNameNode);
			NamespactorActivator.log(toStringDebug(nodeToReplace));
		}
	}

	/**
	 * @return 	true for names inside template specializations (e.g. SC in SC<ClassX> or SC in C<SC<char> >)
	 * */
	protected boolean isPartOfTemplateVariableDeclaration(IASTName childRefNode) {
		return childRefNode.getParent() instanceof ICPPASTTemplateId && NSNodeHelper.findAncestorOf(childRefNode, ICPPASTUsingDeclaration.class) == null;
	}
	
	@Override
	protected void collectModifications(ASTRewriteStore store) {

		for(IASTName nodeToReplace : nodesToReplace.keySet()){
			
			store.addReplaceChange(nodeToReplace, nodesToReplace.get(nodeToReplace));
		}
		
		super.collectModifications(store);
	}
}
