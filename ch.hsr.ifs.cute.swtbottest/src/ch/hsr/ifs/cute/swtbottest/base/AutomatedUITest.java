package ch.hsr.ifs.cute.swtbottest.base;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.inGroup;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withStyle;
import static org.eclipse.swtbot.swt.finder.waits.Conditions.shellCloses;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.function.Consumer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRootMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.PlatformUI;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.BeforeClass;

import ch.hsr.ifs.cute.swtbottest.util.BotConditions;
import ch.hsr.ifs.cute.swtbottest.util.Performer;

/**
 * @author Felix Morgner IFS
 * @author Hansruedi Patzen IFS
 *
 */
public abstract class AutomatedUITest {

	private static final int INDEXER_TIMEOUT = 10000;

	protected static SWTWorkbenchBot fBot = null;
	protected static SWTBotShell fMainShell = null;

	private static String fTestClassName = null;
	private static String fProjectName = null;
	private static String fProjectCategory = null;
	private static String fProjectType = null;

	private static final String RADIO_BUTTON_ALWAYS_OPEN = "Always open";
	private static final String RADIO_GROUP_OPEN_PERSPECTIVE = "Open the associated perspective when creating a new project";

	@BeforeClass
	public static void beforeClass() throws Exception {
//		SWTBotPreferences.PLAYBACK_DELAY = 500;
		SWTBotPreferences.TIMEOUT = 5000;
		fBot = new SWTWorkbenchBot();
//		fBot.sleep(5000);

		fMainShell = findMainShell();

		goFullscreen();
		closeWelcome();
		enableAutomaticPerspectiveChange();
	}

	private static void goFullscreen() {
		UIThreadRunnable.syncExec(() -> {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setFullScreen(true);
		});
	}

	@After
	public void cleanUp() throws Exception {
		expandAllProjects();
		fBot.captureScreenshot("screenshots/" + fTestClassName + "." + fProjectName + ".png");

		for (SWTBotShell shell : fBot.shells()) {
			if (!shell.equals(fMainShell)) {
				String title = shell.getText();
				if (title.length() > 0 && !title.startsWith("Quick Access")) {
					UIThreadRunnable.syncExec(() -> {
						if (shell.widget.getParent() != null && !shell.isOpen()) {
							shell.close();
						}
					});
				}
			}
		}

		fBot.closeAllEditors();
		deleteAllProjects();
		fMainShell.activate();
	}

	private void expandAllProjects() {
		fBot.viewByTitle("Project Explorer");
		for (SWTBotTreeItem item : fBot.tree().getAllItems()) {
			item.expand();
		}
		fBot.sleep(500);
	}

	private void deleteAllProjects() throws Exception {
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			CCorePlugin.getIndexManager().joinIndexer(INDEXER_TIMEOUT, new NullProgressMonitor());
			project.delete(true, new NullProgressMonitor());
		}
	}

	/**
	 * Click on menu item specified by path in the given context of the given
	 * element
	 *
	 * @param element
	 *            An element that has a context menu attached to it
	 * @param path
	 *            The path of the item to click
	 */
	protected static void clickContextMenuEntry(AbstractSWTBot<? extends Widget> element, String... path) {
		SWTBotRootMenu contextMenu = element.contextMenu();
		contextMenu.menu(path).click();
	}

	/**
	 * Click a menu item with the specified path in the given menu contained in the
	 * main shell.
	 *
	 * <p>
	 * This has the same effect as calling
	 * {@link #clickMenuEntry(SWTBotShell, String, String...)} with fMainShell as
	 * the first argument. </p
	 *
	 * @param menu
	 *            The top-level menu
	 * @param path
	 *            The path to the menu item
	 */
	protected static void clickMenuEntry(String menu, String... path) {
		clickMenuEntry(fMainShell, menu, path);
	}

	/**
	 * Click a menu item with the specified path in the given menu that is part of
	 * the provided shell.
	 *
	 * @param shell
	 *            The shell containing the menu
	 * @param menu
	 *            The top-level menu
	 * @param path
	 *            The path to the menu item
	 */
	protected static void clickMenuEntry(SWTBotShell shell, String menu, String... path) {
		shell.setFocus();
		SWTBotMenu shellMenu = shell.menu().menu(menu);
		shellMenu.menu(path).click();
	}

	/**
	 * Get the project with the specified name from the workspace
	 *
	 * @param name
	 *            The desired project's name
	 * @return An {@link #IProject} handle to the project
	 */
	protected static IProject getProject(String name) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertNotNull("Failed to get workspace", workspace);
		IWorkspaceRoot root = workspace.getRoot();
		assertNotNull("Failed to get workspace root", root);
		IProject project = root.getProject(name);
		assertNotNull("Could not find project \"" + name + '"', project);
		return project;
	}

	/**
	 * Get the project specified by the {@link #TestProjectName} specification on
	 * the test function
	 *
	 * @return An {@link #IProject} handle to the project
	 */
	protected static IProject getProject() {
		return getProject(fProjectName);
	}

	/**
	 * Create a project of the given category and type with the given name
	 *
	 * @param type
	 *            The type of the project
	 * @param name
	 *            The name of the project
	 * @return An {@link #IProject} handle to the newly created project
	 */
	protected static IProject createProject(String category, String type, String name) {
		return finalizeProjectCreation(name, prepareProjectCreation(category, type, name));
	}

	/**
	 * Create a project of the given category and type with the given name
	 *
	 * @param type
	 *            The type of the project
	 * @param name
	 *            The name of the project
	 * @param creationHandler
	 *            Custom project creation handler with shell access
	 * @return An {@link #IProject} handle to the newly created project
	 */
	protected static IProject createProject(String category, String type, String name, Consumer<SWTBotShell> creationHandler) {
		SWTBotShell newProjectShell = prepareProjectCreation(category, type, name);
		creationHandler.accept(newProjectShell);
		return finalizeProjectCreation(name, newProjectShell);
	}

	/**
	 * Create a project of the given category and type with the given name
	 *
	 * @param type
	 *            The type of the project
	 * @param name
	 *            The name of the project
	 * @param creationHandler
	 *            Custom project creation handler without shell access
	 * @return An {@link #IProject} handle to the newly created project
	 */
	protected static IProject createProject(String category, String type, String name, Performer creationHandler) {
		SWTBotShell newProjectShell = prepareProjectCreation(category, type, name);
		creationHandler.run();
		return finalizeProjectCreation(name, newProjectShell);
	}

	private static SWTBotShell prepareProjectCreation(String category, String type, String name) {
		clickMenuEntry("File", "New", "Project...");
		SWTBotShell newProjectShell = fBot.shell("New Project");
		fBot.text().setText("C++ Project");
		fBot.waitUntil(BotConditions.selectNodeInTree(fBot.tree(), "C/C++", "C++ Project"));
		fBot.button("Next >").click();
		fBot.textWithLabel("Project name:").setText(name);
		fBot.treeWithLabel("Project type:").expandNode(category).select(type);
		return newProjectShell;
	}

	private static IProject finalizeProjectCreation(String name, SWTBotShell newProjectShell) {
		fBot.button("Finish").click();
		fBot.waitUntil(shellCloses(newProjectShell));
		fBot.sleep(1000);
		return getProject(name);
	}

	/**
	 * Create a project as specified by the {@link #TestProjectCategory},
	 * {@link #TestProjectType} and {@link #TestProjectName} annotations
	 * of the test function
	 *
	 * @return An {@link #IProject} handle to the newly created project
	 */
	protected static IProject createProject() {
		return createProject(fProjectCategory, fProjectType, fProjectName);
	}

	/**
	 * Create a project as specified by the {@link #TestProjectCategory},
	 * {@link #TestProjectType} and {@link #TestProjectName} annotations
	 * of the test function
	 *
	 * @param creationHandler
	 *            Custom project creation handler without shell access
	 * @return An {@link #IProject} handle to the newly created project
	 */
	protected static IProject createProject(Performer creationHandler) {
		return createProject(fProjectCategory, fProjectType, fProjectName, creationHandler);
	}

	/**
	 * Create a project as specified by the {@link #TestProjectCategory},
	 * {@link #TestProjectType} and {@link #TestProjectName} annotations
	 * of the test function
	 *
	 * @param creationHandler
	 *            Custom project creation handler with shell access
	 * @return An {@link #IProject} handle to the newly created project
	 */
	protected static IProject createProject(Consumer<SWTBotShell> creationHandler) {
		return createProject(fProjectCategory, fProjectType, fProjectName, creationHandler);
	}

	protected static IFile getFile(IProject project, String fileName) {
		IFile file = project.getFile(fileName);
		fBot.waitUntil(BotConditions.resourceExists(file));
		return file;
	}

	protected static IFolder getFolder(IProject project, String folderName) {
		IFolder folder = project.getFolder(folderName);
		fBot.waitUntil(BotConditions.resourceExists(folder));
		return folder;
	}

	private static void closeWelcome() {
		try {
			fBot.viewByTitle("Welcome").close();
		} catch (Exception e) {
		}
	}

	private static void enableAutomaticPerspectiveChange() {
		clickMenuEntry("Window", "Preferences");
		fBot.shell("Preferences").activate();
		fBot.sleep(500);
		fBot.text().setText("Perspectives");
		fBot.waitUntil(BotConditions.selectNodeInTree(fBot.tree(), "General", "Perspectives"));
		selectRadioButtonInGroup(RADIO_BUTTON_ALWAYS_OPEN, RADIO_GROUP_OPEN_PERSPECTIVE);
		fBot.button("Apply and Close").click();
	}

	private static SWTBotShell findMainShell() {
		String shellTitle = UIThreadRunnable.syncExec(() -> {
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getText();
		});

		for (SWTBotShell shell : fBot.shells()) {
			if (shell.getText().equals(shellTitle)) {
				return shell;
			}
		}

		fail("No Eclipse shell found!");
		return null;
	}

	@SuppressWarnings("unchecked")
	private static void selectRadioButtonInGroup(String button, String group) {
		UIThreadRunnable.syncExec(() -> {
			Matcher<Button> matcher = allOf(inGroup(group), widgetOfType(Button.class),
					withStyle(SWT.RADIO, "SWT.RADIO"));
			int index = 0;
			while (true) {
				Button radioButton;
				try {
					radioButton = fBot.widget(matcher, index++);
				} catch (IndexOutOfBoundsException e) {
					return;
				}
				if (radioButton.getSelection()) {
					radioButton.setSelection(false);
					return;
				}
			}
		});
		fBot.radioInGroup(button, group).click();
	}
}
