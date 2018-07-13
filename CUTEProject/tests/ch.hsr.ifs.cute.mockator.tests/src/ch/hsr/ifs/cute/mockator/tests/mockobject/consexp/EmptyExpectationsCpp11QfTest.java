package ch.hsr.ifs.cute.mockator.tests.mockobject.consexp;

import org.junit.Ignore;

import ch.hsr.ifs.cute.mockator.tests.AbstractQuickfixTest;
import ch.hsr.ifs.iltis.cpp.core.ast.checker.helper.IProblemId;

import ch.hsr.ifs.cute.mockator.base.misc.IdHelper.ProblemId;
import ch.hsr.ifs.cute.mockator.mockobject.expectations.qf.ConsistentExpectationsQuickFix;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorQuickFix;


public class EmptyExpectationsCpp11QfTest extends AbstractQuickfixTest {

   @Override
   protected IProblemId getProblemId() {
      return ProblemId.INCONSISTENT_EXPECTATIONS;
   }

   @Override
   protected String[] getIncludeDirPaths() {
      return new String[] { "mockator", "cute", "stl" };
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
   protected MockatorQuickFix createMarkerResolution() {
      return new ConsistentExpectationsQuickFix();
   }

   @Override
   @Ignore
   public void runTest() throws Throwable {}

   @Override
   protected String getResolutionMessage() {
      return "- \"Mock()\"<br/>";
   }

   @Override
   protected String[] getMarkerMessages() {
      return null;
   }
}
