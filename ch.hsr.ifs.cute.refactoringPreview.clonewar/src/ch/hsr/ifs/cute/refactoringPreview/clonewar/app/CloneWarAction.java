package ch.hsr.ifs.cute.refactoringPreview.clonewar.app;


import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.cdt.ui.refactoring.actions.RefactoringAction;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;

/**
 * Action to start the refactoring with a wizard, called from the
 * {@link CloneWarActionDelegate}.
 * 
 * @author ythrier(at)hsr.ch
 */
public class CloneWarAction extends RefactoringAction {

    /**
     * Create the {@link CloneWarAction} with a given label.
     * 
     * @param label
     *            Label.
     */
    public CloneWarAction(String label) {
        super(label);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(IShellProvider shellProvider, IWorkingCopy wc,
            ITextSelection s) {
        if (!hasFileResource(wc))
            return;
        CloneWarRefactoringRunner runner = createRunner(shellProvider, wc, s);
        runner.run(new RefactoringASTCache());
    }

    /**
     * Create a refactoring runner for the clonewar refactoring.
     * 
     * @param shellProvider
     *            Shell.
     * @param wc
     *            Working copy.
     * @param selection
     *            Textselection.
     * @return Refactoring runner.
     */
    private CloneWarRefactoringRunner createRunner(
            IShellProvider shellProvider, IWorkingCopy wc,
            ITextSelection selection) {
        return new CloneWarRefactoringRunner((IFile) wc.getResource(),
                selection, null, shellProvider, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(IShellProvider shellProvider, ICElement elem) {
        // TODO Auto-generated method stub

    }

    /**
     * Check if the working copy contains a file as resource.
     * 
     * @param wc
     *            Working copy.
     * @return True if the resource of the working copy is a file, otherwise
     *         false.
     */
    private boolean hasFileResource(IWorkingCopy wc) {
        return (wc.getResource() instanceof IFile);
    }
}
