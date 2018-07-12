package ch.hsr.ifs.mockator.plugin.it.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.PlatformUI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.hsr.ifs.mockator.plugin.project.nature.MockatorNature;

@RunWith(SWTBotJunit4ClassRunner.class)
public class MockatorInCUTEWizardTest extends AbstractMockatorUITest {

	private static final String PROJECT_NAME = "MockatorProj";

	@SuppressWarnings("nls")
	@Before
	public void setup() throws IOException {
		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().forceActive();
			}
		});
	}

	@SuppressWarnings("nls")
	@Test
	public void createCuteWithMockatorProjectByWizard() throws CoreException {
		executeProjectWizard();
		final IProject project = getWorkspaceRoot().getProject(PROJECT_NAME);
		assertNotNull(project);
		assertTrue(project.hasNature(MockatorNature.NATURE_ID));
		assertSrcFolderCreated("mockator", project);
		assertSrcEntryExist("mockator", project);
	}

	private static void assertSrcFolderCreated(final String folderName, final IProject project) {
		final IFolder mockatorFolder = project.getFolder(folderName);
		assertNotNull(mockatorFolder);
	}

	@SuppressWarnings("nls")
	private static void assertSrcEntryExist(final String folderName, final IProject project) {
		boolean mockatorSrcFolderFound = false;

		for (final ICSourceEntry entry : getSourceEntries(project)) {
			if (entry.getName().equals("/" + PROJECT_NAME + "/" + folderName)) {
				mockatorSrcFolderFound = true;
				break;
			}
		}

		assertTrue(mockatorSrcFolderFound);
	}

	private static ICSourceEntry[] getSourceEntries(final IProject project) {
		final ICProjectDescription desc = CCorePlugin.getDefault().getProjectDescription(project, false);
		return desc.getActiveConfiguration().getSourceEntries();
	}

	@SuppressWarnings("nls")
	private void executeProjectWizard() {
		selectCppPerspective();
		final SWTBotShell shell = createNewCppProject();
		createCuteProjectWithMockator(shell);
	}

	private static void createCuteProjectWithMockator(final SWTBotShell shell) {
		bot.textWithLabel("Project name:").setText(PROJECT_NAME);
		bot.checkBox("Show project types and toolchains only if they are supported on the platform").click();
		final SWTBotTree projTypeTree = bot.treeWithLabel("Project type:");
		final SWTBotTreeItem cuteCategory = projTypeTree.getTreeItem("CUTE");
		cuteCategory.select("CUTE Project");
		// SWTBotTree toolChainsTree = bot.treeWithLabel("Toolchains:");
		// toolChainsTree.select("Linux GCC");
		bot.button("Next >").click();
		bot.comboBox().setSelection(0);
		bot.checkBox("Enable mock support with Mockator").click();
		bot.button("Finish").click();
		bot.waitUntil(Conditions.shellCloses(shell), 10000);
	}
}
