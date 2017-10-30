package ch.hsr.ifs.cute.swtbottest.base;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.inGroup;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withStyle;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withText;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.function.Consumer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.PlatformUI;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.BeforeClass;

import ch.hsr.ifs.cute.swtbottest.util.Action;
import ch.hsr.ifs.cute.swtbottest.util.BotConditions;

/**
 * @author Felix Morgner IFS
 * @author Hansruedi Patzen IFS
 *
 */
public abstract class AutomatedUITest {

	protected static SWTWorkbenchBot fBot = null;
	protected static SWTBotShell fMainShell = null;

	private static String fTestClassName = null;
	private static String fProjectName = null;
	private static String fProjectCategory = null;
	private static String fProjectType = null;

	private static final int TEST_TIMEOUT = 10000;
	private static final int INDEXER_TIMEOUT = 20000;

	private static final String RADIO_BUTTON_ALWAYS_OPEN = "Always open";
	private static final String RADIO_GROUP_OPEN_PERSPECTIVE = "Open the associated perspective when creating a new project";

	@BeforeClass
	public static void beforeClass() throws Exception {
		SWTBotPreferences.TIMEOUT = TEST_TIMEOUT;
		fBot = new SWTWorkbenchBot();
		fMainShell = findMainShell();

		goFullscreen();
		closeWelcome();
		enableAutomaticPerspectiveChange();
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

	protected static IProjectNature getProjectNature(ICProject project, String natureId) throws CoreException {
		return project.getProject().getNature(natureId);
	}

	protected static String getProjectName() {
		return getProjectName(getCProject());
	}

	protected static String getProjectName(ICProject project) {
		return project.getProject().getName();
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
		element.contextMenu().menu(path).click();
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
		shell.menu().menu(menu).menu(path).click();
	}

	/**
	 * Get the project with the specified name from the workspace
	 *
	 * @param name
	 *            The desired project's name
	 * @return An {@link #ICProject} handle to the project
	 */
	protected static ICProject getCProject(String name) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertNotNull("Failed to get workspace", workspace);
		IWorkspaceRoot root = workspace.getRoot();
		assertNotNull("Failed to get workspace root", root);
		IProject project = root.getProject(name);
		assertNotNull("Could not find project \"" + name + '"', project);
		return CoreModel.getDefault().create(project);
	}

	/**
	 * Get the project specified by the {@link #TestProjectName} specification on
	 * the test function
	 *
	 * @return An {@link #ICProject} handle to the project
	 */
	protected static ICProject getCProject() {
		return getCProject(fProjectName);
	}

	/**
	 * Create a project of the given category and type with the given name
	 *
	 * @param type
	 *            The type of the project
	 * @param name
	 *            The name of the project
	 * @return An {@link #ICProject} handle to the newly created project
	 */
	protected static ICProject createProject(String category, String type, String name) {
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
	 * @return An {@link #ICProject} handle to the newly created project
	 */
	protected static ICProject createProject(String category, String type, String name, Consumer<SWTBotShell> creationHandler) {
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
	 * @return An {@link #ICProject} handle to the newly created project
	 */
	protected static ICProject createProject(String category, String type, String name, Action creationHandler) {
		SWTBotShell newProjectShell = prepareProjectCreation(category, type, name);
		creationHandler.run();
		return finalizeProjectCreation(name, newProjectShell);
	}

	/**
	 * Create a project as specified by the {@link #TestProjectCategory},
	 * {@link #TestProjectType} and {@link #TestProjectName} annotations
	 * of the test function
	 *
	 * @return An {@link #ICProject} handle to the newly created project
	 */
	protected static ICProject createProject() {
		return createProject(fProjectCategory, fProjectType, fProjectName);
	}

	/**
	 * Create a project as specified by the {@link #TestProjectCategory},
	 * {@link #TestProjectType} and {@link #TestProjectName} annotations
	 * of the test function
	 *
	 * @param creationHandler
	 *            Custom project creation handler without shell access
	 * @return An {@link #ICProject} handle to the newly created project
	 */
	protected static ICProject createProject(Action creationHandler) {
		return createProject(fProjectCategory, fProjectType, fProjectName, creationHandler);
	}

	/**
	 * Create a project as specified by the {@link #TestProjectCategory},
	 * {@link #TestProjectType} and {@link #TestProjectName} annotations
	 * of the test function
	 *
	 * @param creationHandler
	 *            Custom project creation handler with shell access
	 * @return An {@link #ICProject} handle to the newly created project
	 */
	protected static ICProject createProject(Consumer<SWTBotShell> creationHandler) {
		return createProject(fProjectCategory, fProjectType, fProjectName, creationHandler);
	}

	/**
	 * Get a file with the given name inside the project, this methods
	 * ensures that the file exists. Otherwise the test will time out.
	 *
	 * @param project
	 *            Project containing the folder
	 * @param fileName
	 *            Name of the file one is looking for
	 * @return
	 */
	protected static IFile getFile(ICProject project, String fileName) {
		IFile file = project.getProject().getFile(fileName);
		fBot.waitUntil(BotConditions.resourceExists(file));
		return file;
	}

	/**
	 * Get a folder with the given name inside the project, this methods
	 * ensures that the folder exists. Otherwise the test will time out.
	 *
	 * @param project
	 *            Project containing the folder
	 * @param folderName
	 *            Name of the folder one is looking for
	 * @return
	 */
	protected static IFolder getFolder(ICProject project, String folderName) {
		IFolder folder = project.getProject().getFolder(folderName);
		fBot.waitUntil(BotConditions.resourceExists(folder));
		return folder;
	}

	/**
	 * Find a shell with the given text. This method ensures that the shell
	 * exists. Otherwise the test will time out.
	 *
	 * @param shellText
	 *            Text of the shell one is looking for
	 * @return
	 */
	protected static SWTBotShell findShell(String shellText) {
		fBot.waitUntil(Conditions.waitForShell(withText(shellText)));
		return Arrays.stream(fBot.shells())
				.filter(shell -> shell.getText().equals(shellText))
				.findFirst()
				.get();
	}

	/**
	 * Wait for the default project indexer to complete (forces a reindex)
	 */
	protected static void updateIndex() {
		updateIndex(getCProject());
	}

	/**
	 * Wait for the project indexer to complete (forces a reindex)
	 *
	 * @param project
	 *             The project for which the index needs to be up-to-date
	 */
	protected static void updateIndex(ICProject project) {
		forceReindex(project);
		fBot.waitUntil(BotConditions.indexerIsDone(INDEXER_TIMEOUT, project));
	}

	private static void closeWelcome() {
		try {
			fBot.viewByTitle("Welcome").close();
		} catch (Exception e) {
		}
	}

	private static void goFullscreen() {
		UIThreadRunnable.syncExec(() -> {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setFullScreen(true);
		});
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

	private static ICProject finalizeProjectCreation(String name, SWTBotShell newProjectShell) {
		fBot.button("Finish").click();
		fBot.waitUntil(Conditions.shellCloses(newProjectShell));
		return getCProject(name);
	}

	private static void expandAllProjects() {
		fBot.viewByTitle("Project Explorer");
		for (SWTBotTreeItem item : fBot.tree().getAllItems()) {
			item.expand();
//			fBot.waitUntil(BotConditions.expandTreeItem(item));
		}
	}

	private static void deleteAllProjects() throws Exception {
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			project.delete(true, new NullProgressMonitor());
		}
	}

	private static void forceReindex(ICProject project) {
		CCorePlugin.getIndexManager().reindex(project);
		fBot.waitUntil(BotConditions.jobExists("C/C++ Indexer"));
	}

	private static void enableAutomaticPerspectiveChange() {
		clickMenuEntry("Window", "Preferences");
		findShell("Preferences").activate();
		fBot.text().setText("Perspectives");
		fBot.waitUntil(BotConditions.selectNodeInTree(fBot.tree(), "General", "Perspectives"));
		selectRadioButtonInGroup(RADIO_BUTTON_ALWAYS_OPEN, RADIO_GROUP_OPEN_PERSPECTIVE);
		fBot.button("Apply and Close").click();
	}

	private static SWTBotShell findMainShell() {
		String mainShellText = UIThreadRunnable.syncExec(() -> {
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getText();
		});

		return findShell(mainShellText);
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
