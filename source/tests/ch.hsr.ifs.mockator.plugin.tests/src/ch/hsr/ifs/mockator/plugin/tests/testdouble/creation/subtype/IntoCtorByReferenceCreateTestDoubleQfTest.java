package ch.hsr.ifs.mockator.plugin.tests.testdouble.creation.subtype;

import ch.hsr.ifs.iltis.cpp.ast.checker.helper.IProblemId;

import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorQuickFix;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype.CreateTestDoubleSubTypeQuickFix;
import ch.hsr.ifs.mockator.plugin.tests.AbstractQuickfixTest;


public class IntoCtorByReferenceCreateTestDoubleQfTest extends AbstractQuickfixTest {

   @Override
   protected IProblemId getProblemId() {
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
