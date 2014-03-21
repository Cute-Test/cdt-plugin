package ch.hsr.ifs.cute.macronator.refactoring;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringRunner;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.IShellProvider;

@SuppressWarnings("restriction")
public class ExpandMacroRefactoringRunner extends RefactoringRunner {

    public ExpandMacroRefactoringRunner(ICElement element, ISelection selection, IShellProvider shellProvider, ICProject cProject) {
        super(element, selection, shellProvider, cProject);
    }

    @Override
    public void run() {
        ExpandMacroRefactoring refactoring = new ExpandMacroRefactoring(element, selection, project);
        ExpandMacroWizard wizard = new ExpandMacroWizard(refactoring);
        run(wizard, refactoring, RefactoringSaveHelper.SAVE_REFACTORING);
    }
}
