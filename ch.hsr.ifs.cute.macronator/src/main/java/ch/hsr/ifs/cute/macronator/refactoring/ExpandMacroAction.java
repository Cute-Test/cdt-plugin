package ch.hsr.ifs.cute.macronator.refactoring;

import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.refactoring.utils.EclipseObjects;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

@SuppressWarnings("restriction")
public class ExpandMacroAction implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window;
    private ISelection selection;

    @Override
    public void run(IAction action) {
        if (window == null) {
            window = EclipseObjects.getActiveWindow();
        }
        selection = EclipseObjects.getActiveEditor().getEditorSite().getSelectionProvider().getSelection();
        IEditorInput editorInput = EclipseObjects.getActiveEditor().getEditorInput();
        IWorkingCopy workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editorInput);
        new ExpandMacroRefactoringRunner(workingCopy, selection, window, workingCopy.getCProject()).run();
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }
}
