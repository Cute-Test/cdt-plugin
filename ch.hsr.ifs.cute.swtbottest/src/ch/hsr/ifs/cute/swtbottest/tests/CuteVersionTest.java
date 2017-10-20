package ch.hsr.ifs.cute.swtbottest.tests;

import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.hsr.ifs.cute.headers.headers1_7.CuteHeaders_1_7;
import ch.hsr.ifs.cute.headers.headers2_0.CuteHeaders_2_0;
import ch.hsr.ifs.cute.headers.headers2_1.CuteHeaders_2_1;
import ch.hsr.ifs.cute.headers.headers2_2.CuteHeaders_2_2;
import ch.hsr.ifs.cute.swtbottest.annotations.TestProjectCategory;
import ch.hsr.ifs.cute.swtbottest.annotations.TestProjectType;
import ch.hsr.ifs.cute.swtbottest.base.AutomatedUITest;
import ch.hsr.ifs.cute.swtbottest.base.AutomatedUITestRunner;
import ch.hsr.ifs.cute.swtbottest.util.BotConditions;
import ch.hsr.ifs.cute.swtbottest.util.FileUtils;
import ch.hsr.ifs.cute.ui.CuteUIPlugin;
import ch.hsr.ifs.cute.ui.project.headers.ICuteHeaders;

@TestProjectCategory("CUTE")
@RunWith(AutomatedUITestRunner.class)
public class CuteVersionTest extends AutomatedUITest {

	private void assertCuteVersion(IProject project, String cuteVersion) throws Exception {
		String cuteVersionFile = "cute/cute_version.h";
		IFile file = getFile(project, cuteVersionFile);
		String content = FileUtils.getCodeFromIFile(file);
		assertTrue("Cute version " + cuteVersion + " not found in " + cuteVersionFile + ".", content.contains("#define CUTE_LIB_VERSION \"" + cuteVersion + "\""));
	}

	@Test
	@TestProjectType("CUTE Project")
	public void newProjectDefaultVersion() throws Exception {
		final String defaultCuteVersion = "2.2.0";

		IProject project = createProject();
		ICuteHeaders cuteVersion = CuteUIPlugin.getCuteVersion(project);
		assertTrue(cuteVersion instanceof CuteHeaders_2_2);
		assertCuteVersion(project, defaultCuteVersion);
	}

	private void setCuteVersion(final String cuteVersion) {
		fBot.button("Next >").click();
		SWTBotCombo versionComboBox = fBot.comboBoxWithLabel("CUTE Version:");
		fBot.waitUntil(BotConditions.comboBoxHasEntries(versionComboBox));
		versionComboBox.setSelection(cuteVersion);
	}

	@Test
	@TestProjectType("CUTE Project")
	public void newProjectV170() throws Exception {
		final String cuteHeaderVersion = "1.7.0";

		IProject project = createProject(() -> setCuteVersion("CUTE Headers " + cuteHeaderVersion));
		ICuteHeaders cuteVersion = CuteUIPlugin.getCuteVersion(project);
		assertTrue(cuteVersion instanceof CuteHeaders_1_7);
		assertCuteVersion(project, cuteHeaderVersion);
	}

	@Test
	@TestProjectType("CUTE Project")
	public void newProjectV201() throws Exception {
		final String cuteHeaderVersion = "2.0.1";

		IProject project = createProject(() -> setCuteVersion("CUTE Headers " + cuteHeaderVersion));
		ICuteHeaders cuteVersion = CuteUIPlugin.getCuteVersion(project);
		assertTrue(cuteVersion instanceof CuteHeaders_2_0);
		assertCuteVersion(project, cuteHeaderVersion);
	}

	@Test
	@TestProjectType("CUTE Project")
	public void newProjectV210() throws Exception {
		final String cuteHeaderVersion = "2.1.0";

		IProject project = createProject(() -> setCuteVersion("CUTE Headers " + cuteHeaderVersion));
		ICuteHeaders cuteVersion = CuteUIPlugin.getCuteVersion(project);
		assertTrue(cuteVersion instanceof CuteHeaders_2_1);
		assertCuteVersion(project, cuteHeaderVersion);
	}

	@Test
	@TestProjectType("CUTE Project")
	public void newProjectV220() throws Exception {
		final String cuteHeaderVersion = "2.2.0";

		IProject project = createProject(() -> setCuteVersion("CUTE Headers " + cuteHeaderVersion));
		ICuteHeaders cuteVersion = CuteUIPlugin.getCuteVersion(project);
		assertTrue(cuteVersion instanceof CuteHeaders_2_2);
		assertCuteVersion(project, cuteHeaderVersion);
	}
}
