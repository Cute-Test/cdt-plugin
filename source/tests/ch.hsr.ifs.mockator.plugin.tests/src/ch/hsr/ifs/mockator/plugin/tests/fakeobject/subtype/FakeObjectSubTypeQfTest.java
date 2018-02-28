package ch.hsr.ifs.mockator.plugin.tests.fakeobject.subtype;

import ch.hsr.ifs.iltis.cpp.ast.checker.helper.IProblemId;

import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;
import ch.hsr.ifs.mockator.plugin.fakeobject.FakeObjectQuickFix;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorQuickFix;
import ch.hsr.ifs.mockator.plugin.tests.AbstractQuickfixTest;


public class FakeObjectSubTypeQfTest extends AbstractQuickfixTest {

   @Override
   protected IProblemId getProblemId() {
      return ProblemId.SUBTYPE_MISSING_MEMFUNS_IMPL;
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
      return "<b>4 member function(s) to implement</b>:<br/>Fake()<br/>base()<br/>foo()<br/>operator ++()";
   }

   @Override
   protected String[] getMarkerMessages() {
      return new String[] { "Necessary member function(s) not existing in class Fake" };
   }
}
