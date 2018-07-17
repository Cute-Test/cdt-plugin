package ch.hsr.ifs.cute.mockator.tests.mockobject.subtype;

import org.junit.Ignore;

import ch.hsr.ifs.cute.mockator.tests.AbstractQuickfixTest;
import ch.hsr.ifs.iltis.cpp.core.ast.checker.helper.IProblemId;

import ch.hsr.ifs.cute.mockator.base.misc.IdHelper.ProblemId;
import ch.hsr.ifs.cute.mockator.mockobject.qf.MockObjectByFunsQuickFix;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorQuickFix;


public class MockObjectCpp03SubTypeQfTest extends AbstractQuickfixTest {

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
   @Ignore
   public void runTest() throws Throwable {}

   @Override
   protected MockatorQuickFix createMarkerResolution() {
      return new MockObjectByFunsQuickFix();
   }

   @Override
   protected String getResolutionMessage() {
      return "<b>3 member function(s) to implement</b>:<br/>Mock()<br/>base()<br/>foo()";
   }

   @Override
   protected String[] getMarkerMessages() {
      return new String[] { "Necessary member function(s) not existing in class Mock" };
   }
}
