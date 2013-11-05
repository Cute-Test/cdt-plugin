/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.extract;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import ch.hsr.ifs.cute.tdd.TDDPlugin;
import ch.hsr.ifs.cute.tdd.TddHelper;

public class ExtractActionDelegate implements IWorkbenchWindowActionDelegate, IEditorActionDelegate {

	private IWorkbenchWindow window;
	private TextSelection selection;
	private ICProject project;
	private IFile file;

	@Override
	public void run(IAction action) {
		if (!isWorkbenchReady()) {
			return;
		}
		CRefactoringContext context = null;
		try {
			ExtractRefactoring refactoring = new ExtractRefactoring(project.findElement(file.getFullPath()), selection);
			context = new CRefactoringContext(refactoring);
			refactoring.checkAllConditions(new NullProgressMonitor());
			Change change = refactoring.createChange(new NullProgressMonitor());
			change.perform(new NullProgressMonitor());
		} catch (OperationCanceledException e) {
			TddHelper.showErrorOnStatusLine(e.getMessage());
		} catch (CoreException e) {
			TDDPlugin.log("Exception while running extract action", e);
		} finally {
			if (context != null) {
				context.dispose();
			}
		}
	}

	private boolean isWorkbenchReady() {
		IWorkbenchPage activePage = window.getActivePage();
		if (activePage == null)
			return false;
		IEditorPart editor = activePage.getActiveEditor();
		IEditorInput editorInput = editor.getEditorInput();
		if (editor == null || editorInput == null)
			return false;
		IWorkingCopy wc = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editorInput);
		if (wc == null)
			return false;
		project = wc.getCProject();
		file = (IFile) wc.getResource();
		return project != null && file != null;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		boolean isTextSelection = selection != null && selection instanceof TextSelection;
		action.setEnabled(isTextSelection);
		if (isTextSelection) {
			this.selection = (TextSelection) selection;
		}
	}

	@Override
	public void dispose() {
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor != null) {
			this.window = targetEditor.getSite().getWorkbenchWindow();
		}

	}

}
