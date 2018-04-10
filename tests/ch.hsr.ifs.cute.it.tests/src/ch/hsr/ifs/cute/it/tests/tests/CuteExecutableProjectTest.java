package ch.hsr.ifs.cute.it.tests.tests;

import static org.junit.Assert.assertNotNull;

import org.eclipse.cdt.core.model.ICProject;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.hsr.ifs.cute.gcov.GcovNature;
import ch.hsr.ifs.cute.it.tests.annotations.TestProjectCategory;
import ch.hsr.ifs.cute.it.tests.annotations.TestProjectType;
import ch.hsr.ifs.cute.it.tests.base.AutomatedUITest;
import ch.hsr.ifs.cute.it.tests.base.AutomatedUITestRunner;
import ch.hsr.ifs.cute.ui.project.CuteNature;


@TestProjectCategory("CUTE")
@RunWith(AutomatedUITestRunner.class)
public class CuteExecutableProjectTest extends AutomatedUITest {

   @Test
   @TestProjectType("CUTE Project")
   public void newProject() throws Exception {
      ICProject project = createProject();
      assertNotNull("Project does not have CUTE nature", getProjectNature(project, CuteNature.CUTE_NATURE_ID));
   }

   private void setSuiteName() {
      fBot.button("Next >").click();
      fBot.text().setText(getCProject().getProject().getName() + "suite");
   }

   @Test
   @TestProjectType("CUTE Suite Project")
   public void newSuiteProject() throws Exception {
      ICProject project = createProject(this::setSuiteName);
      assertNotNull("Project does not have CUTE nature", getProjectNature(project, CuteNature.CUTE_NATURE_ID));
   }

   private void copyBoostHeaders() {
      fBot.button("Next >").click();
      fBot.checkBox("Copy Boost headers into Project").click();
   }

   @Test
   @TestProjectType("CUTE Project")
   public void newProjectWithBoost() throws Exception {
      ICProject project = createProject(this::copyBoostHeaders);
      assertNotNull("Project does not have CUTE nature", getProjectNature(project, CuteNature.CUTE_NATURE_ID));
      getFolder(project, "boost");
   }

   private void enableGCov() {
      fBot.button("Next >").click();
      fBot.checkBox("Enable coverage analysis using gcov").click();
   }

   @Test
   @TestProjectType("CUTE Project")
   public void newProjectWithGCov() throws Exception {
      ICProject project = createProject(this::enableGCov);
      assertNotNull("Project does not have CUTE nature", getProjectNature(project, CuteNature.CUTE_NATURE_ID));
      assertNotNull("Project does not have GCov nature", getProjectNature(project, GcovNature.NATURE_ID));
   }
}
