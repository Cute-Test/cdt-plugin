/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring2;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ui.ide.IDE;

import ch.hsr.ifs.cute.tdd.createfunction.FunctionCreationHelper;
import ch.hsr.ifs.cute.tdd.createfunction.LinkedModeInformation;

@SuppressWarnings("restriction")
public abstract class CRefactoring3 extends CRefactoring2 {

	private final TextSelection selection;
	protected LinkedModeInformation lmi = new LinkedModeInformation();
	
	public CRefactoring3(ICElement element, ISelection selection,
			RefactoringASTCache astCache) {
		super(element, selection, null, astCache);
		if (!IDE.saveAllEditors(new IResource[] {ResourcesPlugin.getWorkspace().getRoot()}, false)) {
			initStatus.addFatalError(Messages.CRefactoring3_0);
		}
		this.selection = (TextSelection) selection;
	}

	public CRefactoring3(ISelection selection, RefactoringASTCache astCache) {
		this(getEditor().getInputCElement(), selection, astCache);
	}
	
	private static CEditor getEditor() {
		return (CEditor) Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
	}

	public void dispose() {
		astCache.dispose();
	}

	@Override
	protected RefactoringStatus checkFinalConditions(
			IProgressMonitor subProgressMonitor,
			CheckConditionsContext checkContext) throws CoreException,
			OperationCanceledException {
		return initStatus;
	}
	
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return initStatus;
	}

	@Override
	protected RefactoringDescriptor getRefactoringDescriptor() {
		return null;
	}

	@Override
	abstract protected void collectModifications(IProgressMonitor pm,
			ModificationCollector collector) throws CoreException,
			OperationCanceledException;

	public TextSelection getSelection() {
		return selection;
	}

	public void setLinkedModeInformation(IASTTranslationUnit localunit, ICPPASTCompositeTypeSpecifier owningType, IASTDeclaration declaration) {
		lmi.setFileChange(FunctionCreationHelper.setFileChanged(localunit, owningType));
		if (declaration instanceof IASTFunctionDefinition) {
			ICPPASTFunctionDefinition function = (ICPPASTFunctionDefinition) declaration;
			lmi.setReturnStatement(FunctionCreationHelper.hasReturnStatement(function));
			lmi.setIsConst(((ICPPASTFunctionDeclarator)function.getDeclarator()).isConst());
		}
	}
	
	public LinkedModeInformation getLinkedModeInformation() {
		return lmi;
	}
}
