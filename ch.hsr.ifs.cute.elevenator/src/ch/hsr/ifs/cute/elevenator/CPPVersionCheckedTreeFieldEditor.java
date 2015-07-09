package ch.hsr.ifs.cute.elevenator;

import org.eclipse.cdt.codan.internal.ui.preferences.CheckedTreeEditor;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

import ch.hsr.ifs.cute.elevenator.view.DialectBasedSettingsProvider;

public class CPPVersionCheckedTreeFieldEditor extends CheckedTreeEditor {

	public CPPVersionCheckedTreeFieldEditor(String name, String labelText, Composite parent,
			DialectBasedSetting settings) {
		super(name, labelText, parent);
		setEmptySelectionAllowed(true);

		DialectBasedSettingsProvider provider = new DialectBasedSettingsProvider();
		getTreeViewer().setContentProvider(provider);
		getTreeViewer().setLabelProvider(provider);
		getTreeViewer().setInput(settings);
	}

	@Override
	protected void doLoad() {
		System.out.println("doLoad");
		super.doLoad();
	}

	@Override
	protected Object modelFromString(String s) {
		System.out.println("modelFromString");
		return null;
	}

	@Override
	protected String modelToString(Object model) {
		System.out.println("modelToString^");
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
