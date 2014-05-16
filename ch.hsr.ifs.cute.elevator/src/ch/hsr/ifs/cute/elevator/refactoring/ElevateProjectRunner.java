package ch.hsr.ifs.cute.elevator.refactoring;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringRunner;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
@SuppressWarnings("restriction")
public class ElevateProjectRunner extends RefactoringRunner {

    public ElevateProjectRunner(ICElement element, ISelection selection, IShellProvider shellProvider, ICProject cProject) {
        super(element, selection, shellProvider, cProject);
    }

    @Override
    public void run() {
        CRefactoring refactoring = new ElevateProjectRefactoring(element, selection, project);
        RefactoringWizard wizard = new RefactoringWizard(refactoring, RefactoringWizard.WIZARD_BASED_USER_INTERFACE | RefactoringWizard.WIZARD_BASED_USER_INTERFACE) {
            
            @Override
            protected void addUserInputPages() {    
            }
        };
        
        wizard.setDefaultPageTitle("Elevate Project");
        run(wizard, refactoring, RefactoringSaveHelper.SAVE_REFACTORING);
    }
}
