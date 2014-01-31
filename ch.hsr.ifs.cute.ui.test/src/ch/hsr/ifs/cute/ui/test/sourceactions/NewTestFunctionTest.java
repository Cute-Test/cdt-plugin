package ch.hsr.ifs.cute.ui.test.sourceactions;

import java.util.Properties;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingTest;

public class NewTestFunctionTest extends CDTTestingTest {

	private static final String COMMAND_ID = "ch.hsr.ifs.cute.newTestFunctionCommand";
	private String insertText;
	private boolean sendTab;
	private String insertText2;

	@Override
	@Test
	public void runTest() throws Exception {
		openActiveFileInEditor();
		executeCommand(COMMAND_ID);
		if (insertText != null) {
			insertUserTyping(insertText);
		}
		if (sendTab) {
			invokeKeyEvent('\t');
			insertUserTyping(insertText2);
		}
		assertEquals(getExpectedSource(), getCurrentSource());
		closeOpenEditors();
	}

	@Override
	protected void configureTest(Properties properties) {
		insertText = properties.getProperty("insertText");
		sendTab = Boolean.parseBoolean(properties.getProperty("sendTab", "false"));
		insertText2 = properties.getProperty("insertText2");
	}
}
