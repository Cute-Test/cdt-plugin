package ch.hsr.ifs.mockator.plugin.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.cdt.codan.core.model.IProblemReporter;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.junit.Test;

import ch.hsr.ifs.iltis.testing.highlevel.cdttest.CDTTestingQuickfixTest;
import ch.hsr.ifs.iltis.testing.highlevel.cdttest.comparison.ASTComparison.ComparisonArg;
import ch.hsr.ifs.iltis.testing.highlevel.helpers.UIThreadSyncRunnable;

import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorQuickFix;


public abstract class AbstractQuickfixTest extends CDTTestingQuickfixTest {

   @Override
   protected void initAdditionalIncludes() throws Exception {
      stageExternalIncludePathsForBothProjects(getIncludeDirPaths());
      super.initAdditionalIncludes();
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

      //FIXME Fix include-insertion system and then remove IGNORE_INCLUDE_ORDER
      assertAllSourceFilesEqual(EnumSet.of(ComparisonArg.COMPARE_INCLUDE_DIRECTIVES, ComparisonArg.PRINT_CONTEXT_ON_FAIL,
            ComparisonArg.PRINT_WHOLE_ASTS_ON_FAIL, ComparisonArg.IGNORE_INCLUDE_ORDER));
   }

   protected void assertProblemMarkerMessages(final String[] expectedMarkerMessages) throws CoreException {
      assertProblemMarkerMessages(IProblemReporter.GENERIC_CODE_ANALYSIS_MARKER_TYPE, expectedMarkerMessages);
   }

   protected void assertProblemMarkerMessages(final String expectedMarkerId, final String[] expectedMarkerMessages) throws CoreException {
      final List<String> expectedList = new ArrayList<>(Arrays.asList(expectedMarkerMessages));
      final IMarker[] markers = findMarkers(expectedMarkerId);
      for (final IMarker curMarker : markers) {
         final String markerMsg = curMarker.getAttribute("message", null);
         if (expectedList.contains(markerMsg)) {
            expectedList.remove(markerMsg);
         } else {
            fail("marker-message '" + markerMsg + "' not present in given marker message list");
         }
      }
      assertTrue("Not all expected messages found. Remaining: " + expectedList, expectedList.isEmpty());
   }

   protected abstract String[] getMarkerMessages();

   private void assertQfResolutionDescription(final MockatorQuickFix quickfix) {
      assertEquals("Quickfix resolution description mismatch", getResolutionMessage(), quickfix.getDescription());
   }

   private MockatorQuickFix runQuickfix() throws Exception {
      setupCppProject();
      final IMarker[] markers = findMarkers();
      final MockatorQuickFix quickfix = createMarkerResolution();
      quickfix.setRunInCurrentThread(true);
      UIThreadSyncRunnable.run(() -> quickfix.run(markers[0]));
      return quickfix;
   }

   protected abstract String getResolutionMessage();

   protected abstract CppStandard getCppStdToUse();

   protected abstract boolean isManagedBuildProjectNecessary();

   protected abstract boolean isRefactoringUsed();

   @Override
   protected abstract MockatorQuickFix createMarkerResolution();

   private void setupCppProject() throws CoreException {
      if (isManagedBuildProjectNecessary()) {
         new CdtManagedProjectActivator(getCurrentProject()).activateManagedBuild();
         new CdtManagedProjectActivator(getExpectedProject()).activateManagedBuild();

         if (getCppStdToUse() == CppStandard.Cpp11Std) {
            new Cpp11StdActivator(getCurrentProject()).activateCpp11Support();
            new Cpp11StdActivator(getExpectedProject()).activateCpp11Support();
         }
      }
   }
}
