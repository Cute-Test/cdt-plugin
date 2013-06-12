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
package ch.hsr.ifs.cdt.namespactor.refactoring.iudec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.cdt.namespactor.NamespactorActivator;
import ch.hsr.ifs.cdt.namespactor.astutil.ASTNodeFactory;
import ch.hsr.ifs.cdt.namespactor.astutil.NSNameHelper;
import ch.hsr.ifs.cdt.namespactor.astutil.NSNodeHelper;
import ch.hsr.ifs.cdt.namespactor.astutil.NSSelectionHelper;
import ch.hsr.ifs.cdt.namespactor.refactoring.NodeDefinitionNotInWorkspaceException;
import ch.hsr.ifs.cdt.namespactor.refactoring.TemplateIdFactory;
import ch.hsr.ifs.cdt.namespactor.refactoring.iu.IncludeDependencyAnalyser;
import ch.hsr.ifs.cdt.namespactor.refactoring.iu.InlineRefactoringBase;
import ch.hsr.ifs.cdt.namespactor.refactoring.iu.InlineRefactoringContext;
import ch.hsr.ifs.cdt.namespactor.refactoring.iu.InlineTemplateIdFactory;
import ch.hsr.ifs.cdt.namespactor.refactoring.iu.NamespaceInlineContext;
import ch.hsr.ifs.cdt.namespactor.refactoring.rewrite.ASTRewriteStore;
import ch.hsr.ifs.cdt.namespactor.resources.Labels;

/**
 * @author kunz@ideadapt.net
 * */
@SuppressWarnings("restriction")
public class IUDECRefactoring extends InlineRefactoringBase {

	private IncludeDependencyAnalyser includeDepAnalyser = null;
	
	public IUDECRefactoring(ICElement element, ISelection selection, ICProject project) {
		super(element, selection, project);
	}

	@Override
	protected void collectModifications(ASTRewriteStore store) {
		store.addRemoveChange(ctx.selectedUsing);
		super.collectModifications(store);
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);

		super.checkInitialConditions(sm.newChild(6));

		if (initStatus.hasFatalError()) {
			sm.done();
			return initStatus;
		}

		ICPPASTUsingDeclaration selectedUDEC = NSSelectionHelper.getSelectedUsingDeclaration(selectedRegion, getAST(tu, pm));

		if(selectedUDEC == null){
			initStatus.addFatalError(Labels.IUDEC_NoUDECSelected);
		}else{
			ctx.selectedUsing = selectedUDEC;
			ctx.selectedName  = selectedUDEC.getName();
			try {
				includeDepAnalyser = new IncludeDependencyAnalyser(getIndex());
			} catch (CoreException e) {
				e.printStackTrace();
			}

			IBinding decl    = ctx.selectedName.getLastName().resolveBinding();
			IBinding declDel = ((ICPPUsingDeclaration)decl).getDelegates()[0];
			
			if(declDel instanceof ICPPUnknownBinding){
				initStatus.addWarning(String.format(Labels.IUDEC_TemplateArgument));
			}
			
			List<IIndexName> refs = getReferencesOf(declDel);
			try {
				for(IIndexName ref : refs){

					String originFileName = ctx.selectedName.getFileLocation().getFileName();
					if(!includeDepAnalyser.areFilesIncludeDependent(ref.getFile(), originFileName)){
						continue;
					}

					IASTName refNode = getNodeOf(ref, pm);

					if(refNode == null){
						return initStatus;
					}

					if(!isValidChildReference(refNode)){
						continue;
					}

					processReplace(pm, refNode);
				}
			} catch (NodeDefinitionNotInWorkspaceException e) {
				initStatus.addFatalError(Labels.IU_SysNode);
				e.printStackTrace();
			}
		}

		sm.done();
		return initStatus;
	}

	private boolean isValidChildReference(IASTName refNode) throws OperationCanceledException, CoreException {
		
		IASTCompoundStatement enclosingCompoundStatement = NSNodeHelper.findCompoundStatementInAncestors(ctx.selectedUsing);
		if(enclosingCompoundStatement != null){
			if(!NSNodeHelper.isNodeEnclosedBy(enclosingCompoundStatement, refNode)){
				return false;
			}
		}
		
		IBinding refBinding = refNode.resolveBinding();
		if(refBinding instanceof ICPPSpecialization){
			refBinding = ((ICPPSpecialization)refBinding).getSpecializedBinding();
		}
		
		IIndexName[] refNodeDecls = getIndex().findDeclarations(refBinding);
		if(refNodeDecls.length != 1){
			return false;
		}
		
		return true;
	}

	private void processReplace(IProgressMonitor pm, IASTName refNode) throws CoreException, NodeDefinitionNotInWorkspaceException {
		if(isPartOfTemplateVariableDeclaration(refNode)){
			
			initContext(pm, refNode);					
			processTemplateVariableDeclaration(refNode, ctx);
		}else{
		
			addReplacement(getNodeToReplace(refNode), getNewNameNode(refNode, getNodeToReplace(refNode)));
		}
	}

	private List<IIndexName> getReferencesOf(IBinding binding) throws CoreException {
		List<IIndexName> refs = new ArrayList<IIndexName>();
		
		if(binding instanceof ICPPFunctionTemplate && binding instanceof ICPPInstanceCache){
			ICPPTemplateInstance[] instances = ((ICPPInstanceCache)binding).getAllInstances();
			for(ICPPTemplateInstance instance : instances){
				refs.addAll(Arrays.asList(getIndex().findReferences(instance)));
			}
		}
		
		refs.addAll(Arrays.asList(getIndex().findReferences(binding)));
		return refs;
	}
	
	private void initContext(IProgressMonitor pm, IASTName refNode) throws CoreException, NodeDefinitionNotInWorkspaceException {
		ctx.templateIdsToIgnore = new HashSet<ICPPASTTemplateId>();
		IBinding selectedNameBinding = ctx.selectedName.getLastName().resolveBinding();
		
		List<IBinding> childrenBindings = new ArrayList<IBinding>();
		childrenBindings.add(selectedNameBinding);

		IIndexName[] declNames = getIndex().findDeclarations(selectedNameBinding);
		IASTName declNode      = getNodeOf(declNames[0], pm);
		ICPPASTNamespaceDefinition nsDefNode = NSNodeHelper.findAncestorOf(declNode, ICPPASTNamespaceDefinition.class);
		ICPPASTQualifiedName newQName        = ASTNodeFactory.getDefault().newQualifiedName();
		
		if(ctx.selectedName instanceof ICPPASTQualifiedName){
			for(IASTName n : ((ICPPASTQualifiedName) ctx.selectedName).getNames()){
				newQName.addName(n.copy());
			}
		}else{
			newQName.addName(ctx.selectedName.getLastName().copy());
		}
		
		ctx.enclosingNSContext = new NamespaceInlineContext();
		ctx.enclosingNSContext.namespaceDefNode  = nsDefNode;
		ctx.enclosingNSContext.usingName = NSNameHelper.copyQualifers(newQName);
	}

	private IASTName getNodeToReplace(IASTName refNode) {
		if(refNode.getParent() instanceof ICPPASTQualifiedName){
			return (ICPPASTQualifiedName) refNode.getParent();
		}
		return refNode;
	}

	private ICPPASTQualifiedName getNewNameNode(IASTName refNode, IASTName nodeToReplace) {
		ICPPASTQualifiedName newNameNode = null;

		if(ctx.selectedName instanceof ICPPASTQualifiedName){
			newNameNode = (ICPPASTQualifiedName) ctx.selectedName.copy();
		}else{
			newNameNode = ASTNodeFactory.getDefault().newQualifiedName();
			newNameNode.addName(ctx.selectedName.copy());
		}

		addQualifiersAfterRefNode(refNode, nodeToReplace, newNameNode);
		
		return newNameNode;
	}

	private void addQualifiersAfterRefNode(IASTName refNode, IASTName nodeToReplace, ICPPASTQualifiedName newNameNode) {
		if(refNode.getParent() instanceof ICPPASTQualifiedName){
			boolean addNames = false;
			for(IASTName n : ((ICPPASTQualifiedName)nodeToReplace).getNames()){
				if(addNames){
					newNameNode.addName(n.copy());
				}
				if(n.equals(refNode)){
					addNames = true;
				}
			}
		}
	}
	
	@Override
	public void addReplacement(IASTName nodeToReplace, IASTName newNameNode) {
		if(nodeToReplace != null){
			nodesToReplace.put(nodeToReplace, newNameNode);
			NamespactorActivator.log(toStringDebug(nodeToReplace));
		}
	}

	@Override
	protected TemplateIdFactory getTemplateIdFactory(ICPPASTTemplateId templateId, InlineRefactoringContext ctx) {
		return new InlineTemplateIdFactory(templateId, ctx);
	}	
}