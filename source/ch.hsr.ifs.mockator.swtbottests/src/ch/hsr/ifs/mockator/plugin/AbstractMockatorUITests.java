package ch.hsr.ifs.mockator.plugin;

import java.io.IOException;
import java.net.URL;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.WidgetResult;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.BeforeClass;

public abstract class AbstractMockatorUITests {
  protected static SWTWorkbenchBot bot;

  @BeforeClass
  public static void beforeClass() throws Exception {
    bot = new SWTWorkbenchBot();
  }

  @After
  public void cleanup() throws CoreException {
    for (IProject proj : getWorkspaceRoot().getProjects()) {
      JoinableMonitor monitor = new JoinableMonitor();
      proj.delete(true, monitor);
      monitor.join();
    }
  }

  protected SWTBotTree getProjectExplorer() {
    return bot.viewByTitle("Project Explorer").bot().tree();
  }

  protected void importProjectByZip(String zipName) throws IOException {
    bot.menu("File").menu("Import...").click();
    SWTBotShell shell = bot.shell("Import").activate();
    SWTBotTreeItem expandNode = bot.tree().expandNode("General");
    expandNode.select("Existing Projects into Workspace");
    bot.button("Next >").click();
    shell.pressShortcut(SWT.ALT, 'a');
    URL location = MockatorPlugin.getDefault().getBundle().getResource(zipName);
    location = FileLocator.toFileURL(location);
    bot.text(1).setText(location.getPath());
    shell.pressShortcut(SWT.ALT, 'e');
    bot.button("Finish").click();
    bot.waitUntil(Conditions.shellCloses(shell), 10000);
    CCorePlugin.getIndexManager().joinIndexer(10000, new NullProgressMonitor());
  }

  protected IWorkspaceRoot getWorkspaceRoot() {
    return ResourcesPlugin.getWorkspace().getRoot();
  }

  protected SWTBotShell createNewCppProject() {
    bot.menu("File").menu("New").menu("Project...").click();
    SWTBotShell shell = bot.shell("New Project").activate();
    bot.tree().expandNode("C/C++").select("C++ Project");
    bot.button("Next >").click();
    return shell;
  }

  protected SWTBotMenu getSubMenuItem(final SWTBotMenu parentMenu, final String itemText)
      throws WidgetNotFoundException {
    MenuItem menuItem = UIThreadRunnable.syncExec(new WidgetResult<MenuItem>() {
      @Override
      public MenuItem run() {
        Menu bar = parentMenu.widget.getMenu();

        if (bar == null)
          return null;

        for (MenuItem item : bar.getItems()) {
          if (item.getText().equals(itemText))
            return item;
        }

        return null;
      }
    });

    if (menuItem == null)
      throw new WidgetNotFoundException("MenuItem \"" + itemText + "\" not found.");

    return new SWTBotMenu(menuItem);
  }

  protected void selectCppPerspective() {
    bot.perspectiveByLabel("C/C++").activate();
  }

  private static class JoinableMonitor extends NullProgressMonitor {
    private boolean isDone = false;

    @Override
    public synchronized void done() {
      isDone = true;
      notifyAll();
    }

    public synchronized void join() {
      while (!isDone) {
        try {
          wait();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }
}
