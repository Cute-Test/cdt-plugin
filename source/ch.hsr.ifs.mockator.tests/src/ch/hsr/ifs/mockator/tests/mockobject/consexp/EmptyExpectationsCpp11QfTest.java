package ch.hsr.ifs.mockator.tests.mockobject.consexp;

import org.junit.Ignore;

import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.qf.ConsistentExpectationsQuickFix;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorQuickFix;
import ch.hsr.ifs.mockator.tests.MockatorQuickfixTest;


public class EmptyExpectationsCpp11QfTest extends MockatorQuickfixTest {

   @Override
   protected String getProblemId() {
      return ProblemId.INCONSISTENT_EXPECTATIONS.getId();
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
   protected MockatorQuickFix getQuickfix() {
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
