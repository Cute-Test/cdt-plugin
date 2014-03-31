package ch.hsr.ifs.cute.refactoringPreview.clonewar.app;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringRunner;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.view.CloneWarRefactoringWizard;

/**
 * Runner for the refactoring.
 *
 * @author ythrier(at)hsr.ch
 */

public class CloneWarRefactoringRunner extends RefactoringRunner {

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
    public CloneWarRefactoringRunner(ISelection selection,
            ICElement element, IShellProvider shellProvider, ICProject cProject) {
        super(element, selection, shellProvider, cProject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        CRefactoring cloneWarRefactoring = createRefactoring();
        RefactoringWizard cloneWarWizard = createWizard(cloneWarRefactoring);
        run(cloneWarWizard, cloneWarRefactoring, RefactoringSaveHelper.SAVE_REFACTORING);
    }

    /**
     * Create the wizard for the refactoring.
     *
     * @param refactoring
     *            The refactoring.
     * @return Wizard.
     */
    private RefactoringWizard createWizard(CRefactoring refactoring) {
        return new CloneWarRefactoringWizard(refactoring);
    }

    /**
     * Create the clone war refactoring.
     *
     * @return Refactoring.
     */
    private CRefactoring createRefactoring() {
        return new CloneWarRefactoring(selection, element, project);
    }
}
