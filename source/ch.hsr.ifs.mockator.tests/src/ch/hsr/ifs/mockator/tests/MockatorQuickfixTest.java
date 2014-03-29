package ch.hsr.ifs.mockator.tests;

import org.eclipse.core.runtime.CoreException;
import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanQuickfixTest;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorQuickFix;

public abstract class MockatorQuickfixTest extends CDTTestingCodanQuickfixTest {

  @Override
  public void setUp() throws Exception {
    for (String includePath : getIncludeDirPaths()) {
      addIncludeDirPath(includePath);
    }
    super.setUp();
  }

  protected String[] getIncludeDirPaths() {
    return new String[] {};
  }

  @Override
  @Test
  public void runTest() throws Throwable {
    closeWelcomeScreen();
    MockatorQuickFix quickfix = runQuickfix();
    String[] expectedMessages = getMarkerMessages();
    if (expectedMessages != null) {
      assertProblemMarkerMessages(expectedMessages);
    }
    assertQfResolutionDescription(quickfix);
    assertRefactoringResult();
  }

  protected abstract String[] getMarkerMessages();

  private void assertRefactoringResult() {
    new AssertThat(getCurrentSource()).isEqualByIgnoringWhitespace(getExpectedSource());
  }

  private void assertQfResolutionDescription(MockatorQuickFix quickfix) {
    assertEquals("Quickfix resolution description mismatch", getResolutionMessage(),
        quickfix.getDescription());
  }

  private MockatorQuickFix runQuickfix() throws Exception {
    setupCppProject();
    MockatorQuickFix quickfix = getQuickfix();
    quickfix.setRunInCurrentThread(true);
    runQuickFix(quickfix);
    return quickfix;
  }

  protected abstract String getResolutionMessage();

  protected abstract CppStandard getCppStdToUse();

  protected abstract boolean isManagedBuildProjectNecessary();

  protected abstract boolean isRefactoringUsed();

  protected abstract MockatorQuickFix getQuickfix();

  private void setupCppProject() throws CoreException {
    if (isManagedBuildProjectNecessary()) {
      CdtManagedProjectActivator configurator =
          new CdtManagedProjectActivator(cproject.getProject());
      configurator.activateManagedBuild();

      if (getCppStdToUse() == CppStandard.Cpp11Std) {
        new Cpp11StdActivator(cproject.getProject()).activateCpp11Support();
      }
    }
  }
}
