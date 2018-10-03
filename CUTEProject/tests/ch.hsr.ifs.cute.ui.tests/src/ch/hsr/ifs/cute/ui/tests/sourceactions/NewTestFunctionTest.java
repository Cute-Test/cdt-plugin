package ch.hsr.ifs.cute.ui.tests.sourceactions;

import java.util.EnumSet;
import java.util.Properties;

import org.eclipse.ui.handlers.IHandlerService;
import org.junit.Test;

import ch.hsr.ifs.iltis.testing.highlevel.testingplugin.cdttest.base.CDTTestingUITest;
import ch.hsr.ifs.iltis.testing.highlevel.testingplugin.cdttest.comparison.ASTComparison.ComparisonArg;


public class NewTestFunctionTest extends CDTTestingUITest {

    private static final String COMMAND_ID = "ch.hsr.ifs.cute.newTestFunctionCommand";
    private String              insertText;
    private boolean             sendTab;
    private String              insertText2;

    @Test
    public void runTest() throws Exception {
        openPrimaryTestFileInEditor();
        getActiveWorkbenchWindow().getService(IHandlerService.class).executeCommand(COMMAND_ID, null);
        if (insertText != null) {
            insertUserTyping(insertText, getPrimaryIFileFromCurrentProject());
        }
        if (sendTab) {
            invokeKeyEvent('\t');
            insertUserTyping(insertText2, getPrimaryIFileFromCurrentProject());
        }
        saveAllEditors();
        assertAllSourceFilesEqual(EnumSet.of(ComparisonArg.PRINT_WHOLE_ASTS_ON_FAIL));
    }

    @Override
    protected void configureTest(Properties properties) {
        super.configureTest(properties);
        insertText = properties.getProperty("insertText");
        sendTab = Boolean.parseBoolean(properties.getProperty("sendTab", "false"));
        insertText2 = properties.getProperty("insertText2");
    }

}
