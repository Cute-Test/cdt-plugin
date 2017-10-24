package ch.hsr.ifs.cute.swtbottest.util;

import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public final class BotConditions {

	private static final int BOT_TIMEOUT = 100;

	/**
	 * Create a new view text contents matching condition
	 * <p>
	 * The condition evaluates to true if the given pattern is found within the
	 * view's text contents.
	 * </p>
	 *
	 * @param view
	 *            The view to search in
	 * @param textAccessor
	 *            An accessor function to get the view's text part
	 * @param pattern
	 *            The pattern to search for in the view's text contents
	 * @return
	 */
	public static ViewTextContains viewTextContains(SWTBotView view, Supplier<AbstractSWTBotControl<? extends Control>> textAccessor, String pattern) {
		view.setFocus();
		view.bot().sleep(BOT_TIMEOUT);
		return new ViewTextContains(view, textAccessor.get()::getText, pattern);
	}

	/**
	 * Create a new ComboBox has entries condition
	 * <p>
	 * The condition evaluates to true the ComboBox is non empty.
	 * </p>
	 *
	 * @param combobox
	 *            The ComboBox to check for entries
	 * @return
	 */
	public static ComboBoxHasEntries comboBoxHasEntries(SWTBotCombo comboBox) {
		return new ComboBoxHasEntries(comboBox);
	}

	/**
	 * Create a new file contains condition
	 * <p>
	 * The condition evaluates to true if the given file contains the given string
	 * </p>
	 *
	 * @param file
	 *            The file which should contain the needle
	 * @param needle
	 *            The string to look for in the file
	 * @return
	 */
	public static FileContains fileContains(IFile file, String needle) {
		return new FileContains(file, needle);
	}

	/**
	 * Create a new resource exists condition
	 * <p>
	 * The condition evaluates to true if the resource exists
	 * </p>
	 *
	 * @param folder
	 *            The file which should exist
	 * @return
	 */
	public static ResourceExists resourceExists(IResource resource) {
		return new ResourceExists(resource);
	}

	/**
	 * Create a new indexer is done condition
	 * <p>
	 * The condition evaluates to true if the Indexer is done
	 * </p>
	 *
	 * @param timeout
	 *            Timeout for the indexer join job. If the timeout is
	 *            smaller than 0, no timeout will be used.
	 * @return
	 */
	public static IndexerIsDone indexerIsDone(int timeout, ICProject project) {
		return new IndexerIsDone(timeout, project);
	}

	/**
	 * Create a new tree node selection condition
	 *
	 * @param tree
	 *            The tree to select the node in
	 * @param parent
	 *            The parent of the node
	 * @param node
	 *            The node to select
	 * @return A waitable SWTBot condition that selects the desired node.
	 */
	public static TreeNodeIsSelected selectNodeInTree(SWTBotTree tree, String parent, String node) {
		return new TreeNodeIsSelected(tree, parent, node);
	}

	/**
	 * Create a new tree item expansion condition
	 * <p>
	 * The condition evaluates to true a tree node is expanded
	 * </p>
	 *
	 * @param item
	 *            The {@link #SWTBotTreeItem} containing which should be expanded
	 * @return
	 */
	public static TreeItemIsExpanded expandTreeItem(SWTBotTreeItem item) {
		return new TreeItemIsExpanded(item);
	}

	private static final class ViewTextContains extends DefaultCondition {

		private final SWTBotView fView;
		private final Pattern fPattern;
		private final Supplier<String> fAccessor;

		private ViewTextContains(SWTBotView view, Supplier<String> getText, String pattern) {
			fView = view;
			fPattern = Pattern.compile(pattern);
			fAccessor = getText;
		}

		@Override
		public boolean test() throws Exception {
			if (fView.isActive()) {
				String text = fAccessor.get();
				return fPattern.matcher(text).find();
			}
			return false;
		}

		@Override
		public String getFailureMessage() {
			return "Pattern '" + fPattern.pattern() + "' not found!";
		}
	}

	/**
	 * A condition to wait until a SWTBotComboBox has at least one entry
	 */
	private static final class ComboBoxHasEntries extends DefaultCondition {

		final SWTBotCombo fComboBox;

		private ComboBoxHasEntries(SWTBotCombo comboBox) {
			fComboBox = comboBox;
		}

		@Override
		public boolean test() throws Exception {
			return fComboBox.itemCount() > 0;
		}

		@Override
		public String getFailureMessage() {
			return "ComboBox had no entries!";
		}
	}

	/**
	 * A condition to wait until the file contains the needle.
	 */
	private static final class FileContains extends DefaultCondition {
		private final IFile fFile;
		private final String fNeedle;

		private FileContains(IFile file, String needle) {
			fFile = file;
			fNeedle = needle;
		}

		@Override
		public boolean test() throws Exception {
			if (!fFile.isSynchronized(IResource.DEPTH_INFINITE)) {
				fFile.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			}

			String content = FileUtils.getCodeFromIFile(fFile);
			return content.contains(fNeedle);
		}

		@Override
		public String getFailureMessage() {
			return "Needle not found in '" + fFile.getName() + "'.";
		}
	}

	/**
	 * A condition to wait until the file contains the needle.
	 */
	private static final class ResourceExists extends DefaultCondition {
		private final IResource fResource;

		private ResourceExists(IResource file) {
			fResource = file;
		}

		@Override
		public boolean test() throws Exception {
			return fResource.exists();
		}

		@Override
		public String getFailureMessage() {
			return "Unable to find resource '" + fResource.getName() + "'.";
		}
	}

	/**
	 * A condition to wait until the job can be found
	 */
	private static final class IndexerIsDone extends DefaultCondition {

		private final int fTimeout;
		private final ICProject fProject;

		private IndexerIsDone(int timeout, ICProject project) {
			fTimeout = (timeout < 0) ? IIndexManager.FOREVER : timeout;
			fProject = project;
		}

		@Override
		public boolean test() throws Exception {
			final IIndexManager indexManager = CCorePlugin.getIndexManager();
			return indexManager.isProjectIndexed(fProject)
					&& indexManager.isIndexerIdle()
					&& !indexManager.isIndexerSetupPostponed(fProject)
					&& indexManager.joinIndexer(fTimeout, new NullProgressMonitor());

		}

		@Override
		public String getFailureMessage() {
			return "Unable finish indexing within " + fTimeout + "ms.";
		}
	}

	/**
	 * A condition to wait until the file contains the needle.
	 */
	private static final class TreeNodeIsSelected extends DefaultCondition {

		private final SWTBotTree fTree;
		private final String fNode;
		private final String fChild;

		private TreeNodeIsSelected(SWTBotTree tree, String node, String child) {
			fTree = tree;
			fNode = node;
			fChild = child;
		}

		@Override
		public boolean test() throws Exception {
			try {
				SWTBotTreeItem node = fTree.getTreeItem(fNode);
				node.getNode(fChild).select();
				return true;
			} catch (WidgetNotFoundException e) {
				return false;
			}
		}

		@Override
		public String getFailureMessage() {
			return "Failed to select node \"" + fNode + "->" + fChild + "\".";
		}
	}

	/**
	 * A condition to wait until a SWTBotTreeItem is expanded
	 */
	private static final class TreeItemIsExpanded extends DefaultCondition {

		final SWTBotTreeItem fItem;

		private TreeItemIsExpanded(SWTBotTreeItem item) {
			fItem = item;
		}

		@Override
		public boolean test() throws Exception {
			return fItem.isExpanded();
		}

		@Override
		public String getFailureMessage() {
			return "Unable to expand item " + fItem.getText() + ".";
		}
	}
}
