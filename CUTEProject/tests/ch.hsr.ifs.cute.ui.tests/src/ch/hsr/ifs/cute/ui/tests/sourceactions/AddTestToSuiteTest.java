package ch.hsr.ifs.cute.ui.tests.sourceactions;

import java.util.EnumSet;
import java.util.Properties;

import org.eclipse.ui.handlers.IHandlerService;
import org.junit.Test;

import ch.hsr.ifs.iltis.testing.highlevel.testingplugin.cdttest.base.CDTTestingUITest;
import ch.hsr.ifs.iltis.testing.highlevel.testingplugin.cdttest.comparison.ASTComparison.ComparisonArg;


public class AddTestToSuiteTest extends CDTTestingUITest {

   private static final String COMMAND_ID = "ch.hsr.ifs.cute.addTestCommand";
   private boolean             shouldType;
   private String              insertText;
   private int                 insertPosition;

   @Test
   public void runTest() throws Exception {
      openPrimaryTestFileInEditor();
      if (shouldType) {
         insertUserTyping(insertText, getPrimaryIFileFromCurrentProject(), insertPosition);
      }
      getActiveWorkbenchWindow().getService(IHandlerService.class).executeCommand(COMMAND_ID, null);
      assertAllSourceFilesEqual(EnumSet.of(ComparisonArg.PRINT_WHOLE_ASTS_ON_FAIL));
   }

   @Override
   protected void configureTest(Properties properties) {
      if (properties.containsKey("insertText") && properties.containsKey("insertPosition")) {
         shouldType = true;
         insertText = properties.getProperty("insertText");
         insertPosition = Integer.parseInt(properties.getProperty("insertPosition"));
      }
   }

}
