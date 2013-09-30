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
package ch.hsr.ifs.cute.namespactor.ui.eudec;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * @author Jules Weder
 * */
@SuppressWarnings("restriction")
public class EUDECRefactoringActionDelegate implements IWorkbenchWindowActionDelegate, IEditorActionDelegate{
    private IWorkbenchWindow window;
    private static final String ACTION_ID = "ch.hsr.ifs.cute.namespactor.extract";

	@Override
	public void run(IAction action) {

        if (!isEditorCallSource()){
            return;
        }
        EUDECRefactoringAction extractAction = new EUDECRefactoringAction(ACTION_ID);
        extractAction.setEditor((IEditorPart) window.getActivePage().getActivePart());
        extractAction.run();

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		
	}

	@Override
	public void dispose() {

	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
	
    private boolean isEditorCallSource() {
        return (window.getActivePage().getActivePart() instanceof CEditor);
    }

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor != null) {
			this.window = targetEditor.getSite().getWorkbenchWindow();
		}
		
	}

}
