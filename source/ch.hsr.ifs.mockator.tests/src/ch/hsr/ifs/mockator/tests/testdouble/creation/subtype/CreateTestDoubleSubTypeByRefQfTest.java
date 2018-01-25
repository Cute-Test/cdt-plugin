package ch.hsr.ifs.mockator.tests.testdouble.creation.subtype;

import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorQuickFix;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype.CreateTestDoubleSubTypeQuickFix;
import ch.hsr.ifs.mockator.tests.AbstractQuickfixTest;


public class CreateTestDoubleSubTypeByRefQfTest extends AbstractQuickfixTest {

   @Override
   protected String getProblemId() {
      return ProblemId.MISSING_TEST_DOUBLE_SUBTYPE.getId();
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
      return new String[] { "Object seam \"dependency\" cannot be resolved" };
   }
}
