package ch.hsr.ifs.mockator.plugin.project.cutewizard;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.cute.ui.ICuteWizardAdditionHandler;
import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.project.nature.MockatorNature;
import ch.hsr.ifs.mockator.plugin.project.nature.NatureHandler;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;

public class MockatorWizardAdditionHandler implements ICuteWizardAdditionHandler {
  private final boolean withMockatorSupport;
  private final CppStandard cppStd;

  public MockatorWizardAdditionHandler(boolean withMockatorSupport, CppStandard cppStd) {
    this.withMockatorSupport = withMockatorSupport;
    this.cppStd = cppStd;
  }

  @Override
  public void configureProject(IProject project, IProgressMonitor pm) throws CoreException {
    Assert.isTrue(isCppProject(project), "Mockator only supports C++ projects");

    if (withMockatorSupport) {
      setCppStd(project);
      addMockatorNature(project, pm);
    }
  }

  private static boolean isCppProject(IProject project) {
    return new NatureHandler(project).hasNature(CCProjectNature.CC_NATURE_ID);
  }

  private static void addMockatorNature(IProject project, IProgressMonitor pm) throws CoreException {
    MockatorNature.addMockatorNature(project, pm);
  }

  private void setCppStd(IProject project) {
    CppStandard.storeInProjectSettings(project, cppStd);
  }

  @Override
  public void configureLibProject(IProject project) throws CoreException {
    // Nothing necessary
  }
}
