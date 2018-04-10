package ch.hsr.ifs.mockator.plugin.tests.linker.gnuoption;

import ch.hsr.ifs.iltis.cpp.ast.checker.helper.IProblemId;

import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;
import ch.hsr.ifs.mockator.plugin.linker.wrapfun.gnuoption.qf.DeleteWrappedFunctionQuickFix;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorQuickFix;
import ch.hsr.ifs.mockator.plugin.tests.AbstractQuickfixTest;


public class DeleteWrappedFunctionQfTest extends AbstractQuickfixTest {

   @Override
   protected IProblemId getProblemId() {
      return ProblemId.WRAP_FUNCTION;
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
      return new DeleteWrappedFunctionQuickFix();
   }

   @Override
   protected String getResolutionMessage() {
      return "Delete wrapped function";
   }

   @Override
   protected String[] getMarkerMessages() {
      return null; // new String[] {"Manage wrapped function \"_Z3foov\""};
   }
}
