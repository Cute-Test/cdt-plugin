package ch.hsr.ifs.cute.mockator.it.tests;

import java.io.IOException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.ui.PlatformUI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(SWTBotJunit4ClassRunner.class)
public class CProjectsNotSupportedTest extends AbstractMockatorUITest {

    @SuppressWarnings("nls")
    @Before
    public void setup() throws IOException {
        UIThreadRunnable.syncExec(() -> PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().forceActive());
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
