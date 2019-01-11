package ch.hsr.ifs.cute.mockator.it.tests;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.matchers.WithText;
import org.eclipse.swtbot.swt.finder.results.WidgetResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import ch.hsr.ifs.cute.mockator.MockatorPlugin;


@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class AbstractMockatorUITest {

    protected static SWTWorkbenchBot bot;

    @BeforeClass
    public static void beforeClass() throws Exception {
        bot = new SWTWorkbenchBot();
    }

    @AfterClass
    public static void sleep() {
        bot.sleep(2000);
    }

    @After
    public void cleanup() throws CoreException {
        for (final IProject proj : getWorkspaceRoot().getProjects()) {
            proj.delete(true, new NullProgressMonitor());
        }
    }

    protected SWTBotTree getProjectExplorer() {
        return bot.viewByTitle("Project Explorer").bot().tree();
    }

    protected void importProjectByZip(final String zipName) throws IOException {
        bot.menu("File").menu("Import...").click();
        final SWTBotShell shell = bot.shell("Import").activate();
        final SWTBotTreeItem expandNode = bot.tree().expandNode("General");
        expandNode.select("Existing Projects into Workspace");
        bot.button("Next >").click();
        bot.radio("Select archive file:").click();
        //    shell.pressShortcut(SWT.ALT, 'a');
        URL location = MockatorPlugin.getDefault().getBundle().getResource(zipName);
        location = FileLocator.toFileURL(location);
        bot.comboBox(1).setText(location.getPath());
        bot.button("Refresh").click();
        //    shell.pressShortcut(SWT.ALT, 'e');
        bot.button("Finish").click();
        bot.waitUntil(org.eclipse.swtbot.swt.finder.waits.Conditions.shellCloses(shell), 10000);
        CCorePlugin.getIndexManager().joinIndexer(10000, new NullProgressMonitor());
    }

    protected IWorkspaceRoot getWorkspaceRoot() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }

    protected SWTBotShell createNewCppProject() {
        bot.menu("File").menu("New").menu("Project...").click();
        bot.waitUntil(org.eclipse.swtbot.swt.finder.waits.Conditions.waitForShell(WithText.withTextIgnoringCase("New Project")));

        final SWTBotShell shell = bot.shell("New Project")/* .activate() */;
        bot.tree().expandNode("C/C++").select("C++ Project");
        bot.button("Next >").click();
        return shell;
        //TODO add Mockator nature to project
    }

    protected SWTBotMenu getSubMenuItem(final SWTBotMenu parentMenu, final String itemText) throws WidgetNotFoundException {

        final MenuItem menuItem = UIThreadRunnable.syncExec((WidgetResult<MenuItem>) () -> {
            final Menu bar = parentMenu.widget.getMenu();
            if (bar == null) return null;

            for (final MenuItem item : bar.getItems()) {
                if (item.getText().equals(itemText)) return item;
            }

            return null;
        });

        if (menuItem == null) throw new WidgetNotFoundException("MenuItem \"" + itemText + "\" not found.");

        return new SWTBotMenu(menuItem);
    }

    protected void selectCppPerspective() {
        final List<SWTBotView> views = bot.views();
        for (final SWTBotView view : views) {
            if ("Welcome".equals(view.getTitle())) {
                view.close();
            }
        }
        bot.perspectiveByLabel("C/C++").activate();
    }

}
