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
package ch.hsr.ifs.cdt.namespactor.refactoring.eu;


import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.Container;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;

import ch.hsr.ifs.cdt.namespactor.astutil.NSNodeHelper;
import ch.hsr.ifs.cdt.namespactor.astutil.NSSelectionHelper;
import ch.hsr.ifs.cdt.namespactor.refactoring.RefactoringBase;
import ch.hsr.ifs.cdt.namespactor.refactoring.rewrite.ASTRewriteStore;
import ch.hsr.ifs.cdt.namespactor.resources.Labels;

/**
 * @author Jules Weder
 * */
@SuppressWarnings("restriction")
public abstract class EURefactoring extends RefactoringBase {

	protected IASTNode scopeNode;
	protected IASTTranslationUnit astTU;
	protected EURefactoringContext context = new EURefactoringContext();

	public EURefactoring(ICElement element, ISelection selection,ICProject project) {
		super(element, selection, project);
	}

	protected static ICPPASTQualifiedName getSelectedQualifiedName(final Region textSelection, IASTTranslationUnit tu) {

		if(textSelection.getLength() > 0){
			final Container<ICPPASTQualifiedName> container = new Container<ICPPASTQualifiedName>();

			tu.getDeclarations();

			tu.accept(new ASTVisitor() {
				{
					shouldVisitNames = true;
				}

				@Override
				public int visit(IASTName name) {
					if (name instanceof ICPPASTQualifiedName && NSSelectionHelper.isSelectionOnExpression(textSelection, name)) {
						ICPPASTQualifiedName selection = NSSelectionHelper.getInnerMostSelectedNameInExpression(textSelection, (ICPPASTQualifiedName)name);
						if(selection != null && !NSSelectionHelper.isSelectionCandidate(name)){
								return super.visit(name);
						}else{
							container.setObject(selection);
						}
					}
					return PROCESS_SKIP;
				}
			});

			return container.getObject();
		}

		return null;
	}

	protected abstract IASTNode prepareInsertStatement();

	protected abstract void findStartingNames(EURefactoringContext context);

	protected abstract ICPPASTQualifiedName buildUsingNameFrom(ICPPASTQualifiedName lastName);

	protected abstract EUReplaceVisitor getReplaceVisitor();

	@Override
	public void collectModifications(IProgressMonitor pm, ModificationCollector collector) throws CoreException, OperationCanceledException {
		ASTRewriteStore store = new ASTRewriteStore(collector);

		addReplaceChanges(store);

		addInsertChange(store);

		addRemoveChanges(store);

		store.performChanges();

	}

	private void addRemoveChanges(ASTRewriteStore store) {
		for (IASTNode node : context.nodesToRemove) {
			store.addRemoveChange(node);		
		}
	}

	private void addInsertChange(ASTRewriteStore store) {
		if(context.firstNameToReplace != null){
			IASTNode insertionPoint = findInsertionPoint(context.firstNameToReplace);

			IASTNode statement = prepareInsertStatement();

			store.addInsertChange(scopeNode, statement, insertionPoint);
		}
	}

	private void addReplaceChanges(ASTRewriteStore store) {
		for (IASTNode nodeToReplace : context.nodesToReplace.keySet()) {
			store.addReplaceChange(nodeToReplace, context.nodesToReplace.get(nodeToReplace));
		}
	}

	@Override
	protected RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext checkContext) throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);

		super.checkInitialConditions(sm.newChild(6));

		if (initStatus.hasFatalError()) {
			sm.done();
			return initStatus;
		}

		astTU = getAST(tu, pm);
		context.selectedQualifiedName = getSelectedQualifiedName(selectedRegion, astTU);

		if(context.selectedQualifiedName == null){
			initStatus.addFatalError(Labels.No_QName_Selected);
			sm.done();
			return initStatus;
		}

		context.selectedLastName = context.selectedQualifiedName.getLastName();
		context.qualifiedUsingName = buildUsingNameFrom(context.selectedQualifiedName);

		if(context.qualifiedUsingName == null){
			initStatus.addFatalError(Labels.No_Using_Name_Built);
			sm.done();
			return initStatus;
		}

		findStartingNames(context);

		if(context.startingNamespaceName == null && !context.selectedQualifiedName.isFullyQualified()){
			initStatus.addFatalError(Labels.No_QName_Selected);
			sm.done();
			return initStatus;
		}

		scopeNode = findScope();
		if(initStatus.hasFatalError()){
			return initStatus;
		}

		acceptReplaceVisitor();

		sm.done();
		return initStatus;
	}
	
	protected abstract IASTNode findTypeScope();

	protected void acceptReplaceVisitor() {
		scopeNode.accept(getReplaceVisitor());
	}

	protected IASTNode findInsertionPoint(IASTNode insertNode) {

		IASTNode insertionPoint = insertNode;
		
		while(!insertionPoint.getParent().equals(scopeNode)){
			insertionPoint = insertionPoint.getParent();
		}

		return insertionPoint;
	}

	protected IASTNode findNamespaceScope() {
		IASTNode scopeNode = null;
		if(context.selectedLastName.resolveBinding() instanceof ICPPClassType){
			scopeNode = NSNodeHelper.findAncestorOf(context.selectedQualifiedName, ICPPASTNamespaceDefinition.class);
		}
		return scopeNode;
	}

	protected IASTNode findScope() {
		IASTNode scope = findCompoundScope();
		if(scope == null){
			scope = findTypeScope();
		}
	
		if(initStatus.hasFatalError()){
			return null;
		}
	
		if(scope == null){
			scope = findNamespaceScope();
		}
	
		if(scope == null){
			scope = astTU;
		}
		return scope;
	}

	private IASTCompoundStatement findCompoundScope() {

		return NSNodeHelper.findCompoundStatementInAncestors(context.selectedLastName);
	}
}