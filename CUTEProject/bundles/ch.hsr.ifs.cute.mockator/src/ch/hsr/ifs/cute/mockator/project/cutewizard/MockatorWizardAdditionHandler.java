package ch.hsr.ifs.cute.mockator.project.cutewizard;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.iltis.core.exception.ILTISException;

import ch.hsr.ifs.cute.mockator.project.nature.MockatorNature;
import ch.hsr.ifs.cute.mockator.project.nature.NatureHandler;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.ui.ICuteWizardAdditionHandler;


public class MockatorWizardAdditionHandler implements ICuteWizardAdditionHandler {

    private final boolean     withMockatorSupport;
    private final CppStandard cppStd;

    public MockatorWizardAdditionHandler(final boolean withMockatorSupport, final CppStandard cppStd) {
        this.withMockatorSupport = withMockatorSupport;
        this.cppStd = cppStd;
    }

    @Override
    public void configureProject(final IProject project, final IProgressMonitor pm) throws CoreException {
        ILTISException.Unless.isTrue("Mockator only supports C++ projects", isCppProject(project));

        if (withMockatorSupport) {
            setCppStd(project);
            addMockatorNature(project, pm);
        }
    }

    private static boolean isCppProject(final IProject project) {
        return new NatureHandler(project).hasNature(CCProjectNature.CC_NATURE_ID);
    }

    private static void addMockatorNature(final IProject project, final IProgressMonitor pm) throws CoreException {
        MockatorNature.addMockatorNature(project, pm);
    }

    private void setCppStd(final IProject project) {
        CppStandard.storeInProjectSettings(project, cppStd);
    }

    @Override
    public void configureLibProject(final IProject project) throws CoreException {
        // Nothing necessary
    }
}
