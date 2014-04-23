package ch.hsr.ifs.cute.elevator.refactoring;

import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class ElevateProjectAction implements IWorkbenchWindowActionDelegate {

    @Override
    public void run(IAction action) {
        ISelection selection = CUIPlugin.getActivePage().getSelection();
        IEditorInput editorInput = CUIPlugin.getActivePage().getActiveEditor().getEditorInput();
        IWorkingCopy workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editorInput);
        new ElevateProjectRunner(workingCopy, selection, CUIPlugin.getActiveWorkbenchWindow(), workingCopy.getCProject()).run();
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public void init(IWorkbenchWindow window) {
        
    }
}
