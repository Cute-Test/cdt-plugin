package ch.hsr.ifs.mockator.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.CoreException;
import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanQuickfixTest;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorQuickFix;


public abstract class AbstractQuickfixTest extends CDTTestingCodanQuickfixTest {

   @Override
   public void setUp() throws Exception {
      for (final String includePath : getIncludeDirPaths()) {
         addIncludeDirPath(includePath);
      }
      super.setUp();
   }

   protected String[] getIncludeDirPaths() {
      return new String[] {};
   }

   @Test
   public void runTest() throws Throwable {
      closeWelcomeScreen();
      final MockatorQuickFix quickfix = runQuickfix();
      final String[] expectedMessages = getMarkerMessages();
      if (expectedMessages != null) {
         assertProblemMarkerMessages(expectedMessages);
      }
      assertQfResolutionDescription(quickfix);

      /*
       * FIXME this comparison should compare includes, but somehow the call to Cpp11StdActivator.activateCpp11Support() adds a load of messed up
       * includes. -> expected must create the same context as actual!
       */
      assertEqualsAST(getExpectedAST(), getCurrentAST(), true, false);
   }

   protected abstract String[] getMarkerMessages();

   private void assertQfResolutionDescription(final MockatorQuickFix quickfix) {
      assertEquals("Quickfix resolution description mismatch", getResolutionMessage(), quickfix.getDescription());
   }

   private MockatorQuickFix runQuickfix() throws Exception {
      setupCppProject();
      final MockatorQuickFix quickfix = getQuickfix();
      quickfix.setRunInCurrentThread(true);
      runQuickFix(quickfix);
      return quickfix;
   }

   protected abstract String getResolutionMessage();

   protected abstract CppStandard getCppStdToUse();

   protected abstract boolean isManagedBuildProjectNecessary();

   protected abstract boolean isRefactoringUsed();

   protected abstract MockatorQuickFix getQuickfix();

   private void setupCppProject() throws CoreException {
      if (isManagedBuildProjectNecessary()) {
         final CdtManagedProjectActivator configurator = new CdtManagedProjectActivator(cproject.getProject());
         configurator.activateManagedBuild();

         if (getCppStdToUse() == CppStandard.Cpp11Std) {
            new Cpp11StdActivator(cproject.getProject()).activateCpp11Support();
         }
      }
   }
}
