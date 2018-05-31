package ch.hsr.ifs.cute.ui.tests.sourceactions;

import java.util.Properties;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingTest;


public class AddTestToSuiteTest extends CDTTestingTest {

   private static final String COMMAND_ID = "ch.hsr.ifs.cute.addTestCommand";
   private boolean             shouldType;
   private String              insertText;
   private int                 insertPosition;

   @Override
   @Test
   public void runTest() throws Exception {
      openActiveFileInEditor();
      if (shouldType) {
         insertUserTyping(insertText, insertPosition);
      }
      executeCommand(COMMAND_ID);
      assertEquals(getExpectedSource(), getCurrentSource());
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
