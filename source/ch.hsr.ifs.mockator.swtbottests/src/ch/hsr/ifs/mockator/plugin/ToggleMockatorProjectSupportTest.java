package ch.hsr.ifs.mockator.plugin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.hsr.ifs.mockator.plugin.project.nature.MockatorNature;


@RunWith(SWTBotJunit4ClassRunner.class)
public class ToggleMockatorProjectSupportTest extends AbstractMockatorUITests {

   @SuppressWarnings("nls")
   @Before
   public void setup() throws IOException {
      selectCppPerspective();
      importProjectByZip("MockatorProject.zip");
   }

   @SuppressWarnings("nls")
   @Test
   public void toggleMockatorSupportYieldsNatureExistence() throws Exception {
      enableMockatorSupport();
      final IProject project = getWorkspaceRoot().getProject("MockatorProject");
      assertTrue(project.hasNature(MockatorNature.NATURE_ID));
      removeMockatorSupport();
      assertFalse(project.hasNature(MockatorNature.NATURE_ID));
   }

   private void enableMockatorSupport() {
      getSubMenuItem(getMockatorContextMenu(), "&Add Mockator Support").click();
   }

   private void removeMockatorSupport() {
      getSubMenuItem(getMockatorContextMenu(), "&Remove Mockator Support").click();
   }

   private SWTBotMenu getMockatorContextMenu() {
      final SWTBotTree tree = getProjectExplorer();
      tree.select("MockatorProject");
      return tree.contextMenu("Mockator");
   }
}
