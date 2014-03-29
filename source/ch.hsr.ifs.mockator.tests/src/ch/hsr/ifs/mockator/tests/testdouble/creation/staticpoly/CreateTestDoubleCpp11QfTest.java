package ch.hsr.ifs.mockator.tests.testdouble.creation.staticpoly;

import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorQuickFix;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.staticpoly.CreateTestDoubleStaticPolyQuickFix;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.staticpoly.MissingTestDoubleStaticPolyChecker;
import ch.hsr.ifs.mockator.tests.MockatorQuickfixTest;

public class CreateTestDoubleCpp11QfTest extends MockatorQuickfixTest {

  @Override
  protected String getProblemId() {
    return MissingTestDoubleStaticPolyChecker.MISSING_TEST_DOUBLE_STATICPOLY_PROBLEM_ID;
  }

  @Override
  protected CppStandard getCppStdToUse() {
    return CppStandard.Cpp11Std;
  }

  @Override
  protected boolean isManagedBuildProjectNecessary() {
    return true;
  }

  @Override
  protected boolean isRefactoringUsed() {
    return true;
  }

  @Override
  protected MockatorQuickFix getQuickfix() {
    return new CreateTestDoubleStaticPolyQuickFix();
  }

  @Override
  protected String getResolutionMessage() {
    return null;
  }

  @Override
  protected String[] getMarkerMessages() {
    return new String[] {"Compile seam \"Fake\" cannot be resolved"};
  }
}
