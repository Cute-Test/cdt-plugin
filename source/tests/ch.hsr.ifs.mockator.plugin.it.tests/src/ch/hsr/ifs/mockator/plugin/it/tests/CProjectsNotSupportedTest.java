package ch.hsr.ifs.mockator.plugin.it.tests;

import java.io.IOException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(SWTBotJunit4ClassRunner.class)
public class CProjectsNotSupportedTest extends AbstractMockatorUITest {

   @SuppressWarnings("nls")
   @Before
   public void setup() throws IOException {
      selectCppPerspective();
      importProjectByZip("EmptyCProject.zip");
   }

   @SuppressWarnings("nls")
   @Test(expected = WidgetNotFoundException.class)
   public void emptyCProjectHasNoMockatorMenu() {
      final SWTBotTree tree = getProjectExplorer();
      tree.select("EmptyCProject");
      tree.contextMenu("Mockator");
   }
}
