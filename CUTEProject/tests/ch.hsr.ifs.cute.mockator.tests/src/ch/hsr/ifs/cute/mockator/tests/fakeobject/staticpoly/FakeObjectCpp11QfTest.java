package ch.hsr.ifs.cute.mockator.tests.fakeobject.staticpoly;

import org.junit.Ignore;

import ch.hsr.ifs.cute.mockator.fakeobject.FakeObjectQuickFix;
import ch.hsr.ifs.cute.mockator.ids.IdHelper.ProblemId;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorQuickFix;
import ch.hsr.ifs.cute.mockator.tests.AbstractQuickfixTest;


public class FakeObjectCpp11QfTest extends AbstractQuickfixTest {

   @Override
   protected ProblemId getProblemId() {
      return ProblemId.STATIC_POLY_MISSING_MEMFUNS_IMPL;
   }

   @Override
   protected CppStandard getCppStdToUse() {
      return CppStandard.Cpp11Std;
   }

   @Override
   protected String[] getIncludeDirPaths() {
      return new String[] { "stl" };
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
   @Ignore
   public void runTest() throws Throwable {}

   @Override
   protected MockatorQuickFix createMarkerResolution() {
      return new FakeObjectQuickFix();
   }

   @Override
   protected String getResolutionMessage() {
      return "<b>1 member function(s) to implement</b>:<br/>foo(const std::map&lt;int,int&gt;&amp;) const";
   }

   @Override
   protected String[] getMarkerMessages() {
      return new String[] { "Necessary member function(s) not existing in class Fake" };
   }
}
