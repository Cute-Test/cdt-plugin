package ch.hsr.ifs.mockator.tests.fakeobject.staticpoly;

import ch.hsr.ifs.mockator.plugin.fakeobject.FakeObjectQuickFix;
import ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.StaticPolymorphismChecker;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorQuickFix;
import ch.hsr.ifs.mockator.tests.MockatorQuickfixTest;


public class FakeObjectCpp03QfTest extends MockatorQuickfixTest {

   @Override
   protected String getProblemId() {
      return StaticPolymorphismChecker.STATIC_POLY_MISSING_MEMFUNS_IMPL_PROBLEM_ID;
   }

   @Override
   protected CppStandard getCppStdToUse() {
      return CppStandard.Cpp03Std;
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
      return new FakeObjectQuickFix();
   }

   @Override
   protected String getResolutionMessage() {
      return "<b>4 member function(s) to implement</b>:<br/>foo(const a::A&amp;) const<br/>" +
             "foo1(const std::string&amp;) const<br/>foo2(const std::string&amp;) const<br/>operator ++()";
   }

   @Override
   protected String[] getMarkerMessages() {
      return new String[] { "Necessary member function(s) not existing in class Fake" };
   }
}
