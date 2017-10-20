package ch.hsr.ifs.cute.swtbottest.tests;

import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.hsr.ifs.cute.swtbottest.annotations.TestProjectCategory;
import ch.hsr.ifs.cute.swtbottest.annotations.TestProjectType;
import ch.hsr.ifs.cute.swtbottest.base.AutomatedUITest;
import ch.hsr.ifs.cute.swtbottest.base.AutomatedUITestRunner;
import ch.hsr.ifs.cute.swtbottest.util.BotConditions;
import ch.hsr.ifs.cute.swtbottest.util.FileUtils;
import ch.hsr.ifs.cute.swtbottest.util.WaitUtils;

@TestProjectCategory("CUTE")
@RunWith(AutomatedUITestRunner.class)
public class CuteSuiteFileTest extends AutomatedUITest {

	private static final String TEST_CPP_FILENAME = "src/Test.cpp";

	private void setSuiteName() {
		fBot.button("Next >").click();
		fBot.text().setText(getProject().getName() + "suite");
	}

	@Test
	@TestProjectType("CUTE Suite Project")
	public void newLinkedSuiteFileTest() throws Exception {
		IProject project = createProject(this::setSuiteName);

		WaitUtils.waitForIndexer(fBot);

		SWTBotTreeItem srcFolder = fBot.tree().getTreeItem(getProject().getName()).expand().getNode("src").select();

		clickContextMenuEntry(srcFolder, "New", "CUTE Suite File");

		final String fileName = "LinkedSuiteFile";

		fBot.textWithLabel("Suite name:").setText(fileName);
		fBot.checkBox("Link to runner ").select();
		fBot.waitUntil(BotConditions.comboBoxHasEntries(fBot.comboBoxWithLabel("Choose run method")));
		fBot.button("Finish").click();

		IFile testFile = getFile(project, TEST_CPP_FILENAME);
		getFile(project, "src/" + fileName+ ".h");
		getFile(project, "src/" + fileName + ".cpp");

		fBot.waitUntil(BotConditions.fileContains(testFile, "#include \"" + fileName + ".h\""));

		String content = FileUtils.getCodeFromIFile(testFile);
		assertTrue(content.contains("cute::suite " + fileName + " = make_suite_" + fileName + "();"));
		assertTrue(content.contains("success &= runner(" + fileName + ", \"" + fileName + "\");"));
	}

	@Test
	@TestProjectType("CUTE Suite Project")
	public void newSuiteFileTest() throws Exception {

		final IProject project = createProject(this::setSuiteName);

		WaitUtils.waitForIndexer(fBot);

		SWTBotTreeItem srcFolder = fBot.tree().getTreeItem(getProject().getName()).expand().getNode("src").select();

		clickContextMenuEntry(srcFolder, "New", "CUTE Suite File");

		final String fileName = "SuiteFile";

		fBot.textWithLabel("Suite name:").setText(fileName);
		fBot.button("Finish").click();

		getFile(project, TEST_CPP_FILENAME);

		IFile file = getFile(project, "src/" + fileName + ".h");
		fBot.waitUntil(BotConditions.fileContains(file, "#include \"cute_suite.h\""));
		String content = FileUtils.getCodeFromIFile(file);

		assertTrue(content.contains("extern cute::suite make_suite_" + fileName + "();"));

		file = getFile(project, "src/" + fileName + ".cpp");
		fBot.waitUntil(BotConditions.fileContains(file, "#include \"" + fileName + ".h\""));
		content = FileUtils.getCodeFromIFile(file);

		assertTrue(content.contains("#include \"cute.h\""));
		assertTrue(content.contains("void thisIsA" + fileName + "Test() {"));
		assertTrue(content.contains("cute::suite make_suite_" + fileName + "() {"));
		assertTrue(content.contains("s.push_back(CUTE(thisIsA" + fileName + "Test));"));
	}
}
