package ch.hsr.ifs.cute.mockator.tests.testdouble.creation.staticpoly;

import ch.hsr.ifs.cute.mockator.ids.IdHelper.ProblemId;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorQuickFix;
import ch.hsr.ifs.cute.mockator.testdouble.creation.staticpoly.CreateTestDoubleStaticPolyQuickFix;
import ch.hsr.ifs.cute.mockator.tests.AbstractQuickfixTest;


public class CreateTestDoubleCpp03QfTest extends AbstractQuickfixTest {

   @Override
   protected ProblemId getProblemId() {
      return ProblemId.MISSING_TEST_DOUBLE_STATICPOLY;
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
   protected MockatorQuickFix createMarkerResolution() {
      return new CreateTestDoubleStaticPolyQuickFix();
   }

   @Override
   protected String getResolutionMessage() {
      return null;
   }

   @Override
   protected String[] getMarkerMessages() {
      return new String[] { "Compile seam \"Fake\" cannot be resolved" };
   }

}
