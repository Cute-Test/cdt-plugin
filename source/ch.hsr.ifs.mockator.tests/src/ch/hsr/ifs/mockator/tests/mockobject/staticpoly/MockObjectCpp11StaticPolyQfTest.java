package ch.hsr.ifs.mockator.tests.mockobject.staticpoly;

import ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.StaticPolymorphismChecker;
import ch.hsr.ifs.mockator.plugin.mockobject.qf.MockObjectByFunsQuickFix;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorQuickFix;
import ch.hsr.ifs.mockator.tests.MockatorQuickfixTest;

public class MockObjectCpp11StaticPolyQfTest extends MockatorQuickfixTest {

  @Override
  protected String getProblemId() {
    return StaticPolymorphismChecker.STATIC_POLY_MISSING_MEMFUNS_IMPL_PROBLEM_ID;
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
    return new MockObjectByFunsQuickFix();
  }

  @Override
  protected String getResolutionMessage() {
    return "<b>3 member function(s) to implement</b>:<br/>Mock()<br/>foo() const<br/>operator ++(int)";
  }

  @Override
  protected String[] getMarkerMessages() {
    return new String[] {"Necessary member function(s) not existing in class Mock"};
  }
}
