package ch.hsr.ifs.cute.ui.tests.sourceactions;

import java.util.Properties;

import org.eclipse.ui.handlers.IHandlerService;
import org.junit.Test;

import ch.hsr.ifs.iltis.testing.highlevel.testingplugin.cdttest.base.CDTTestingUITest;


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
      saveAllEditors();
      assertAllSourceFilesEqual(COMPARE_AST_AND_COMMENTS_AND_INCLUDES);
   }

   @Override
   protected void configureTest(Properties properties) {
      super.configureTest(properties);
      if (properties.containsKey("insertText") && properties.containsKey("insertPosition")) {
         shouldType = true;
         insertText = properties.getProperty("insertText");
         insertPosition = Integer.parseInt(properties.getProperty("insertPosition"));
      }
   }

}
