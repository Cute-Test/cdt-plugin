package ch.hsr.ifs.mockator.plugin;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.Before;
import org.junit.Test;


public class CProjectsNotSupportedTest extends AbstractMockatorUITests {

   @SuppressWarnings("nls")
   @Before
   public void setup() throws IOException {
      selectCppPerspective();
      importProjectByZip("EmptyCProject.zip");
   }

   @SuppressWarnings("nls")
   @Test(expected = TimeoutException.class)
   public void emptyCProjectHasNoMockatorMenu() throws Exception {
      final SWTBotTree tree = getProjectExplorer();
      tree.select("EmptyCProject");
      tree.contextMenu("Mockator");
   }
}
