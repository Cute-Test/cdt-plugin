/******************************************************************************
 * Copyright (c) 2015 Institute for Software, HSR Hochschule fuer Technik 
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 *
 * Contributors:
 * 	Peter Sommerlad peter.sommerlad@hsr.ch
 ******************************************************************************/
package ch.hsr.ifs.cute.namespactor.refactoring.itda;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.util.StringUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTypedef;
import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.cdt.internal.ui.refactoring.Container;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.cute.namespactor.astutil.NSSelectionHelper;
import ch.hsr.ifs.cute.namespactor.refactoring.RefactoringBase;
import ch.hsr.ifs.cute.namespactor.refactoring.rewrite.ASTRewriteStore;
import ch.hsr.ifs.cute.namespactor.resources.Labels;

/**
 * @author peter.sommerlad@hsr.ch
 * */
@SuppressWarnings("restriction")
public class ITDARefactoring extends RefactoringBase {

	public static class FindTypeAliasName implements ASTRunnable {
		private final Region textSelection;
		private Region selectedTypeDefRegion;
		private IType theUnderlyingType;
		private IASTNode theNodeToReplace;

		public IType getTheType() {
			return theUnderlyingType;
		}

		public FindTypeAliasName(Region textSelection) {
			this.textSelection = textSelection;
			this.theNodeToReplace=null;
			this.theUnderlyingType=null;
		}

		public Region getSelectedTypeDefRegion() {
			return selectedTypeDefRegion;
		}

		@Override
		public IStatus runOnAST(ILanguage lang, IASTTranslationUnit astRoot) throws CoreException {
			IASTNodeSelector selector= astRoot.getNodeSelector(null);
			IASTName name= selector.findEnclosingName(textSelection.getOffset(), textSelection.getLength());
			if (name==null)
				return Status.CANCEL_STATUS;
			// find enclosing declaration and if it is a typedef, adjust region.
			IBinding thebinding = ((ICPPASTName) name).resolveBinding();
			
			if (thebinding instanceof CPPTypedef){
				theUnderlyingType = ((CPPTypedef) thebinding).getType();
				theNodeToReplace = name;
				return Status.OK_STATUS;
			} else {
				return Status.CANCEL_STATUS;
			}
				
		}

		public IASTNode getTheNodeToReplace() {
			return theNodeToReplace;
		}

	}
	private IType theUnderlyingType;
	private IASTNode theNodeToReplace;
	private static FindTypeAliasName runner; 
	
	public ITDARefactoring(ICElement element, ISelection selection, ICProject project) {
		super(element, selection, project);
		if (selection instanceof TextSelection && ((TextSelection) selection).getOffset()<0){
			System.err.println("TD2ARefactoring text selection invalid from marker resolution");
		}
	}

	public IASTNode getNodeToReplace(final Region textSelection, IASTTranslationUnit tu) {

		runner = new FindTypeAliasName(textSelection);
		try {
			runner.runOnAST(null, tu);
		} catch (CoreException e) {
		}
		if (runner.getTheNodeToReplace()!=null) {
			theUnderlyingType = runner.getTheType();
			return runner.getTheNodeToReplace();
		}
		// now search using a visitor, if shortcut above doesn't succeed.
		final Container<IASTNode> container = new Container<IASTNode>();
		final Region selection = runner.getSelectedTypeDefRegion()!=null?
				runner.getSelectedTypeDefRegion() : textSelection;

		tu.accept(new ASTVisitor() {
			{
				shouldVisitNames = true;
			}

			@Override
			public int leave(IASTName name) {
				// find enclosing simple declaration and then check if selection is here
				// and find the typedef SimpleDeclaration if it actually is one
				IASTNode node=name;
				if (container.getObject() == null 
					&& NSSelectionHelper.isSelectionOnExpression(selection, name)){
					IBinding thebinding = ((ICPPASTName) name).resolveBinding();
					
						if (thebinding instanceof CPPTypedef){

							container.setObject(node);
							return ASTVisitor.PROCESS_ABORT; // done container is filled
						}
				}
				return super.leave(name);
			}

			@Override
			public int visit(IASTName name) {
				return super.visit(name);
			}
		});
		if (container.getObject() != null){
			IBinding thebinding = ((ICPPASTName) container.getObject()).resolveBinding();
			if (thebinding instanceof CPPTypedef){
				theUnderlyingType=((CPPTypedef) thebinding).getType();
			}
		}
		return container.getObject();
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);

		super.checkInitialConditions(sm.newChild(6));

		if (initStatus.hasFatalError()) {
			sm.done();
			return initStatus;
		}

		theNodeToReplace= getNodeToReplace(selectedRegion, getAST(tu, pm));

		if (theNodeToReplace == null) {
			initStatus.addFatalError(Labels.TD2A_NoNameSelected);
		} 

		sm.done();
		return initStatus;
	}


	@Override
	protected void collectModifications(ASTRewriteStore store) {
		String newTypeName = ASTTypeUtil.getType(theUnderlyingType, false);
		ICPPNodeFactory factory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
		if (theUnderlyingType instanceof ICPPBinding) {
			try {
				String[] qName = ((ICPPBinding) theUnderlyingType).getQualifiedName();
				newTypeName = StringUtil.join(qName, "::");
			} catch (DOMException e) {}
		}
		IASTName replacement = factory.newName(newTypeName.toCharArray());
		store.addReplaceChange(theNodeToReplace, replacement  );
		super.collectModifications(store);
	}


}