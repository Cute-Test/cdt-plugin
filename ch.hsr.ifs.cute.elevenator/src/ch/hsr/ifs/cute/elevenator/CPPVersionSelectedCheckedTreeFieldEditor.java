package ch.hsr.ifs.cute.elevenator;

import org.eclipse.cdt.codan.internal.ui.preferences.CheckedTreeEditor;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

public class CPPVersionSelectedCheckedTreeFieldEditor extends CheckedTreeEditor {
	public CPPVersionSelectedCheckedTreeFieldEditor() {
		super();
	}

	public CPPVersionSelectedCheckedTreeFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
	}

	@Override
	protected Object modelFromString(String s) {
		return null;
	}

	@Override
	protected String modelToString(Object model) {
		return null;
	}

	@Override
	protected CheckboxTreeViewer doCreateTreeViewer(Composite parent, int defaultStyle) {
		PatternFilter filter = new PatternFilter();
		filter.setIncludeLeadingWildcard(true);

		FilteredTree filteredTree = new FilteredTree(parent,
				SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION, filter, true) {
			@Override
			protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
				return new CheckboxTreeViewer(parent, style);
			}
		};
		return (CheckboxTreeViewer) filteredTree.getViewer();
	}

}
