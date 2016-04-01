package ch.hsr.ifs.templator.plugin.view.interfaces;

import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.hsr.ifs.templator.plugin.view.tree.TreeEntry;
import ch.hsr.ifs.templator.plugin.viewdata.ViewData;

public interface ITreeViewController {
	void reflow();

	void addSubEntry(TreeEntry parent, int entryIndex, ViewData data);

	void closeEntry(TreeEntry treeEntry);

	void closeAllSubEntries(TreeEntry treeEntry);

	void minimizeAllSubEntries(TreeEntry treeEntry);

	void maximizeAllSubEntries(TreeEntry treeEntry);

	void entryScrolled(TreeEntry treeEntry);

	ScrolledForm getForm();

	void minimizeAll();

	void maximizeAll();

	void scrollToEntry(TreeEntry treeEntry);

	void refreshFromEditor();

	void clear();
}
