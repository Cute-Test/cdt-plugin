package ch.hsr.ifs.cute.refactoringPreview.clonewar.app;

import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring2;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringRunner2;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;

import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.view.CloneWarRefactoringWizard;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.view.Messages;

/**
 * Runner for the refactoring.
 * 
 * @author ythrier(at)hsr.ch
 */
@SuppressWarnings("restriction")
public class CloneWarRefactoringRunner extends RefactoringRunner2 {

    /**
     * Create the runner with the given arguments.
     * 
     * @param file
     *            File.
     * @param selection
     *            Selection.
     * @param element
     *            Element.
     * @param shellProvider
     *            Shell.
     * @param cProject
     *            Project.
     */
    public CloneWarRefactoringRunner(IFile file, ISelection selection,
            ICElement element, IShellProvider shellProvider, ICProject cProject) {
        super(element, selection, shellProvider, cProject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(RefactoringASTCache astCache) {
        CRefactoring2 cloneWarRefactoring = createRefactoring(astCache);
        RefactoringWizard cloneWarWizard = createWizard(cloneWarRefactoring);
        RefactoringWizardOpenOperation openOperation = createOpenOperation(cloneWarWizard);
        IIndex index = null;
        try {
            index = astCache.getIndex();
//            index.acquireReadLock();
            openOperation.run(getShell(), Messages.STARTUP_ERROR_MSG);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (CoreException e) {
            CUIPlugin.log(e);
        } finally {
            if(index != null){
//                index.releaseReadLock();
            }
        }
    }

    /**
     * Return the shell for the refactoring.
     * 
     * @return Shell.
     */
    private Shell getShell() {
        return shellProvider.getShell();
    }

    /**
     * Create an open operation for the wizard.
     * 
     * @param wizard
     *            The wizard.
     * @return Open operation.
     */
    private RefactoringWizardOpenOperation createOpenOperation(
            RefactoringWizard wizard) {
        return new RefactoringWizardOpenOperation(wizard);
    }

    /**
     * Create the wizard for the refactoring.
     * 
     * @param refactoring
     *            The refactoring.
     * @return Wizard.
     */
    private RefactoringWizard createWizard(CRefactoring2 refactoring) {
        return new CloneWarRefactoringWizard(refactoring);
    }

    /**
     * Create the clone war refactoring.
     * 
     * @return Refactoring.
     */
    private CRefactoring2 createRefactoring(RefactoringASTCache astCache) {
        return new CloneWarRefactoring(selection, element, project, astCache);
    }
}
