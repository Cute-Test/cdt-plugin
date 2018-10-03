/*******************************************************************************
 * Copyright (c) 2007-2015, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.actions;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.hsr.ifs.cute.ui.dialogs.ChangeCuteVersionWizard;


/**
 * @author egraf
 * @since 4.0
 *
 */
public class ChangeCuteVersionAction extends AbstractHandler implements IWorkbenchWindowActionDelegate {

    IProject project;

    @Override
    public void dispose() {}

    @Override
    public void init(IWorkbenchWindow window) {}

    @Override
    public void run(IAction action) {
        changeVersion();
    }

    private void changeVersion() {
        Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
        ChangeCuteVersionWizard wizard = new ChangeCuteVersionWizard(project);

        WizardDialog dialog = new WizardDialog(shell, wizard);
        dialog.create();
        dialog.open();
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        updateSelection(selection);
    }

    private void updateSelection(ISelection selection) {
        if (selection instanceof TreeSelection) {
            TreeSelection treeSel = (TreeSelection) selection;
            if (treeSel.getFirstElement() instanceof IProject) {
                project = (IProject) treeSel.getFirstElement();
            }
            if (treeSel.getFirstElement() instanceof ICProject) {
                ICProject cproject = (ICProject) treeSel.getFirstElement();
                project = cproject.getProject();
            }
        }
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        updateSelection(selection);
        changeVersion();
        return null;
    }
}
