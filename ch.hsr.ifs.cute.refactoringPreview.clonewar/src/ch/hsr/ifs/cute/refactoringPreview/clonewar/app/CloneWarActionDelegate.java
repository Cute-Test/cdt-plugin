package ch.hsr.ifs.cute.refactoringPreview.clonewar.app;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.view.Messages;

/**
 * Starting the refactoring process after getting the run command from the menu.
 * 
 * @author ythrier(at)hsr.ch
 */
@SuppressWarnings("restriction")
public class CloneWarActionDelegate implements IWorkbenchWindowActionDelegate, IEditorActionDelegate {
    private IWorkbenchWindow window;

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(IAction action) {
        if (!isCallFromEditor()) // Just allowing calls from editor.
            return;
        CloneWarAction cloneWarAction = new CloneWarAction(
                Messages.STARTUP_RUNNER_MSG);
        cloneWarAction.setEditor(window.getActivePage().getActiveEditor());
        cloneWarAction.run();
    }

    /**
     * Check if the refactoring was called from the editor.
     * 
     * @return True if the call came from the editor, otherwise false.
     */
    private boolean isCallFromEditor() {
        return (window.getActivePage().getActivePart() instanceof CEditor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        /** no selection **/
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        /** nothing to dispose **/
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

    @Override
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        if(targetEditor != null){
            this.window = targetEditor.getSite().getWorkbenchWindow();
        }
    }
}
