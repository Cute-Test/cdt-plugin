package ch.hsr.ifs.cute.mockator.tests.testdouble.creation.subtype;

import ch.hsr.ifs.cute.mockator.ids.IdHelper.ProblemId;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorQuickFix;
import ch.hsr.ifs.cute.mockator.testdouble.creation.subtype.CreateTestDoubleSubTypeQuickFix;
import ch.hsr.ifs.cute.mockator.tests.AbstractQuickfixTest;


public class IntoMemberFunctionByReferenceCreateTestDoubleQfTest extends AbstractQuickfixTest {

   @Override
   protected ProblemId getProblemId() {
      return ProblemId.MISSING_TEST_DOUBLE_SUBTYPE;
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
   protected MockatorQuickFix createMarkerResolution() {
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
