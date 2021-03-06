package ch.hsr.ifs.cute.mockator.extractinterface.ui;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;


class ExtractInterfaceWizard extends RefactoringWizard {

    public ExtractInterfaceWizard(final Refactoring refactoring) {
        super(refactoring, WIZARD_BASED_USER_INTERFACE);
        setForcePreviewReview(true);
    }

    @Override
    protected void addUserInputPages() {
        addPage(new ExtractInterfaceWizardPage("Extract Interface"));
    }
}
