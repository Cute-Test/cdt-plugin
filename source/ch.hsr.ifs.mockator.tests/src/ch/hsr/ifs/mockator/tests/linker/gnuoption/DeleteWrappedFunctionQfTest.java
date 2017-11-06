package ch.hsr.ifs.mockator.tests.linker.gnuoption;

import ch.hsr.ifs.mockator.plugin.linker.wrapfun.gnuoption.GnuOptionChecker;
import ch.hsr.ifs.mockator.plugin.linker.wrapfun.gnuoption.qf.DeleteWrappedFunctionQuickFix;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorQuickFix;
import ch.hsr.ifs.mockator.tests.MockatorQuickfixTest;


public class DeleteWrappedFunctionQfTest extends MockatorQuickfixTest {

   @Override
   protected String getProblemId() {
      return GnuOptionChecker.WRAP_FUNCTION_PROBLEM_ID;
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
