package ch.hsr.ifs.cute.swtbottest.tests;

import static org.junit.Assert.assertNotNull;

import org.eclipse.core.resources.IProject;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.hsr.ifs.cute.gcov.GcovNature;
import ch.hsr.ifs.cute.swtbottest.annotations.TestProjectCategory;
import ch.hsr.ifs.cute.swtbottest.annotations.TestProjectType;
import ch.hsr.ifs.cute.swtbottest.base.AutomatedUITest;
import ch.hsr.ifs.cute.swtbottest.base.AutomatedUITestRunner;
import ch.hsr.ifs.cute.ui.project.CuteNature;

@TestProjectCategory("CUTE")
@RunWith(AutomatedUITestRunner.class)
public class CuteExecutableProjectTest extends AutomatedUITest {

	@Test
	@TestProjectType("CUTE Project")
	public void newProject() throws Exception {
		IProject project = createProject();
		assertNotNull("Project does not have CUTE nature", project.getNature(CuteNature.CUTE_NATURE_ID));
	}

	private void setSuiteName() {
		fBot.button("Next >").click();
		fBot.text().setText(getProject().getName() + "suite");
	}

	@Test
	@TestProjectType("CUTE Suite Project")
	public void newSuiteProject() throws Exception {
		IProject project = createProject(this::setSuiteName);
		assertNotNull("Project does not have CUTE nature", project.getNature(CuteNature.CUTE_NATURE_ID));
	}

	private void copyBoostHeaders() {
		fBot.button("Next >").click();
		fBot.checkBox("Copy Boost headers into Project").click();
	}

	@Test
	@TestProjectType("CUTE Project")
	public void newProjectWithBoost() throws Exception {
		IProject project = createProject(this::copyBoostHeaders);
		assertNotNull("Project does not have CUTE nature", project.getNature(CuteNature.CUTE_NATURE_ID));
		getFolder(project, "boost");
	}

	private void enableGCov() {
		fBot.button("Next >").click();
		fBot.checkBox("Enable coverage analysis using gcov").click();
	}

	@Test
	@TestProjectType("CUTE Project")
	public void newProjectWithGCov() throws Exception {
		IProject project = createProject(this::enableGCov);
		assertNotNull("Project does not have CUTE nature", project.getNature(CuteNature.CUTE_NATURE_ID));
		assertNotNull("Project does not have GCov nature", project.getNature(GcovNature.NATURE_ID));
	}
}
