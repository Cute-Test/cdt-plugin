package ch.hsr.ifs.cute.it.tests.tests;

import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.hsr.ifs.cute.core.headers.CuteHeaders;
import ch.hsr.ifs.cute.headers.versions.CuteHeaders_1_7;
import ch.hsr.ifs.cute.headers.versions.CuteHeaders_2_0;
import ch.hsr.ifs.cute.headers.versions.CuteHeaders_2_1;
import ch.hsr.ifs.cute.headers.versions.CuteHeaders_2_2;
import ch.hsr.ifs.cute.it.tests.annotations.TestProjectCategory;
import ch.hsr.ifs.cute.it.tests.annotations.TestProjectType;
import ch.hsr.ifs.cute.it.tests.base.AutomatedUITest;
import ch.hsr.ifs.cute.it.tests.base.AutomatedUITestRunner;
import ch.hsr.ifs.cute.it.tests.util.BotConditions;
import ch.hsr.ifs.cute.it.tests.util.FileUtils;
import ch.hsr.ifs.cute.ui.CuteUIPlugin;


@TestProjectCategory("CUTE")
@RunWith(AutomatedUITestRunner.class)
public class CuteVersionTest extends AutomatedUITest {

   private void assertCuteVersion(ICProject project, String cuteVersion) throws Exception {
      String cuteVersionFile = "cute/cute_version.h";
      IFile file = getFile(project, cuteVersionFile);
      String content = FileUtils.getCodeFromIFile(file);
      assertTrue("Cute version " + cuteVersion + " not found in " + cuteVersionFile + ".", content.contains("#define CUTE_LIB_VERSION \"" +
                                                                                                            cuteVersion + "\""));
   }

   @Test
   @TestProjectType("CUTE Project")
   public void newProjectDefaultVersion() throws Exception {
      final String defaultCuteVersion = "2.2.1";

      ICProject project = createProject();
      CuteHeaders cuteVersion = CuteUIPlugin.getCuteVersion(project.getProject());
      assertTrue(cuteVersion instanceof CuteHeaders_2_2);
      assertCuteVersion(project, defaultCuteVersion);
   }

   private void setCuteVersion(final String cuteVersion) {
      fBot.button("Next >").click();
      SWTBotCombo versionComboBox = fBot.comboBoxWithLabel("CUTE Version:");
      fBot.waitUntil(BotConditions.comboBoxHasEntries(versionComboBox));
      versionComboBox.setSelection(cuteVersion);
   }

   private static final String CUTE_HEADERS_PREFIX = "CUTE Headers ";

   @Test
   @TestProjectType("CUTE Project")
   public void newProjectV170() throws Exception {
      final String cuteHeaderVersion = "1.7.0";

      ICProject project = createProject(() -> setCuteVersion(CUTE_HEADERS_PREFIX + cuteHeaderVersion));
      CuteHeaders cuteVersion = CuteUIPlugin.getCuteVersion(project.getProject());
      assertTrue(cuteVersion instanceof CuteHeaders_1_7);
      assertCuteVersion(project, cuteHeaderVersion);
   }

   @Test
   @TestProjectType("CUTE Project")
   public void newProjectV201() throws Exception {
      final String cuteHeaderVersion = "2.0.1";

      ICProject project = createProject(() -> setCuteVersion(CUTE_HEADERS_PREFIX + cuteHeaderVersion));
      CuteHeaders cuteVersion = CuteUIPlugin.getCuteVersion(project.getProject());
      assertTrue(cuteVersion instanceof CuteHeaders_2_0);
      assertCuteVersion(project, cuteHeaderVersion);
   }

   @Test
   @TestProjectType("CUTE Project")
   public void newProjectV211() throws Exception {
      final String cuteHeaderVersion = "2.1.1";

      ICProject project = createProject(() -> setCuteVersion(CUTE_HEADERS_PREFIX + cuteHeaderVersion));
      CuteHeaders cuteVersion = CuteUIPlugin.getCuteVersion(project.getProject());
      assertTrue(cuteVersion instanceof CuteHeaders_2_1);
      assertCuteVersion(project, cuteHeaderVersion);
   }

   @Test
   @TestProjectType("CUTE Project")
   public void newProjectV221() throws Exception {
      final String cuteHeaderVersion = "2.2.1";

      ICProject project = createProject(() -> setCuteVersion(CUTE_HEADERS_PREFIX + cuteHeaderVersion));
      CuteHeaders cuteVersion = CuteUIPlugin.getCuteVersion(project.getProject());
      assertTrue(cuteVersion instanceof CuteHeaders_2_2);
      assertCuteVersion(project, cuteHeaderVersion);
   }
}