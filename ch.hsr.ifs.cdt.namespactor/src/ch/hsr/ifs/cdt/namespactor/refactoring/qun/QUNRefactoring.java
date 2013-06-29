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

import java.util.HashSet;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
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
/*				if(isPartOfTemplateVariableDeclaration(selectedName)){
					initContext(selectedName);
					processTemplateVariableDeclaration(selectedName, ctx);

				} else*/ if(selectedNameBinding instanceof ICPPSpecialization){

					selectedNameBinding = ((ICPPSpecialization) selectedNameBinding).getSpecializedBinding();
				}
//selectedNameBinding.
				addReplacement(selectedName, ASTNodeFactory.getDefault().newQualifiedNameNode(NSNameHelper.getQualifiedName(selectedNameBinding)));
			
		}

		sm.done();
		return initStatus;
	}
	private void initContext(IASTName selectedName) throws CoreException {
		ctx.selectedName        = selectedName;
		ctx.enclosingCompound = NSNodeHelper.findCompoundStatementInAncestors(selectedName);
		ctx.templateIdsToIgnore = new HashSet<ICPPASTTemplateId>();
		IBinding selectedNameBinding = ctx.selectedName.resolveBinding();
		IBinding owner = selectedNameBinding.getOwner();
		IIndexName[] declNames = getIndex().findDeclarations(selectedNameBinding);
		IASTName declNode      = null;
		ICPPASTNamespaceDefinition nsDefNode=null;
		ICPPASTCompositeTypeSpecifier classDefNode=null; // sorry, no common baseclass, getName() is required for nsDefNode
		if (declNames.length>0){
			try {
				declNode=getNodeOf(declNames[0], npm);
			} catch (NodeDefinitionNotInWorkspaceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			nsDefNode= NSNodeHelper.findAncestorOf(declNode, ICPPASTNamespaceDefinition.class);
			classDefNode= NSNodeHelper.findAncestorOf(declNode, ICPPASTCompositeTypeSpecifier.class);
		}
		ICPPASTQualifiedName newQName        = ASTNodeFactory.getDefault().newQualifiedNameNode(NSNameHelper.getQualifiedName(selectedNameBinding));

		  
		ctx.enclosingNSContext = new NamespaceInlineContext();
		ctx.enclosingNSContext.namespaceDefNode  = nsDefNode;
		ctx.enclosingNSContext.classDefNode  = classDefNode;
		ctx.enclosingNSContext.usingName = NSNameHelper.copyQualifers(newQName);
		ctx.enclosingNSContext.namespaceDefBinding = owner;
		ctx.enclosingNSContext.namespaceDefName = getIndex().findDefinitions(owner)[0];
/*		IBinding decl    = ctx.selectedName.getLastName().resolveBinding();
		IBinding targetDeclarationBinding = ((ICPPUsingDeclaration)decl).getDelegates()[0]; // OK?
		targetDeclarationBinding= getIndex().adaptBinding(targetDeclarationBinding);

		
		
		IIndexName[] declNames = getIndex().findDeclarations(selectedNameBinding);
		ICPPASTNamespaceDefinition nsDefNode = NSNodeHelper.findAncestorOf(getNodeOf(declNames[0], npm), ICPPASTNamespaceDefinition.class);// AOB
																																			// Exc....
		ICPPASTQualifiedName newQName        = ASTNodeFactory.getDefault().newQualifiedNameNode(CPPVisitor.getQualifiedName(selectedNameBinding));
		
		ctx.enclosingNSContext = new NamespaceInlineContext();
		ctx.enclosingNSContext.namespaceDefNode  = nsDefNode;
		ctx.enclosingNSContext.usingName         = NSNameHelper.copyQualifers(newQName);
*/	}
	
	@Override
	protected TemplateIdFactory getTemplateIdFactory(ICPPASTTemplateId templateId, InlineRefactoringContext ctx) {
		return new QUNTemplateIdFactory(templateId, ctx);
	}	
	@Override
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

}