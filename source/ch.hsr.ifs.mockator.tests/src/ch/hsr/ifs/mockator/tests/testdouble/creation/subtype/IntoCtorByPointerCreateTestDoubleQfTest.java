package ch.hsr.ifs.mockator.tests.testdouble.creation.subtype;

import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorQuickFix;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype.CreateTestDoubleSubTypeQuickFix;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype.MissingTestDoubleSubTypeChecker;
import ch.hsr.ifs.mockator.tests.MockatorQuickfixTest;


public class IntoCtorByPointerCreateTestDoubleQfTest extends MockatorQuickfixTest {

   @Override
   protected String getProblemId() {
      return MissingTestDoubleSubTypeChecker.MISSING_TEST_DOUBLE_SUBTYPE_PROBLEM_ID;
   }

   @Override
   protected CppStandard getCppStdToUse() {
      return CppStandard.Cpp03Std;
   }

   @Override
   protected boolean isManagedBuildProjectNecessary() {
      return false;
   }

   @Override
   protected boolean isRefactoringUsed() {
      return true;
   }

   @Override
   protected MockatorQuickFix getQuickfix() {
      return new CreateTestDoubleSubTypeQuickFix();
   }

   @Override
   protected String getResolutionMessage() {
      return null;
   }

   @Override
   protected String[] getMarkerMessages() {
      return new String[] { "Object seam \"foo\" cannot be resolved" };
   }
}
