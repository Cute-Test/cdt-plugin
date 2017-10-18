/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.wizard;

import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CElementContentProvider;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.ui.CElementSorter;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;

/**
 * @since 4.0
 */
public class SourceFolderSelectionDialog extends ElementTreeSelectionDialog {

	private static final Class<?>[] VALIDATOR_CLASSES = new Class<?>[] { ICContainer.class, ICProject.class };
	private static final TypedElementSelectionValidator fValidator = new TypedElementSelectionValidator(VALIDATOR_CLASSES, false);

	private static final Class<?>[] FILTER_CLASSES = new Class<?>[] { ICModel.class, ICContainer.class, ICProject.class };
	private static final ViewerFilter fFilter = new TypedViewerFilter(FILTER_CLASSES);

	private static final ViewerComparator fSorter = new CElementSorter();

	public SourceFolderSelectionDialog(Shell parent) {
		super(parent, createLabelProvider(), createContentProvider());
		setValidator(fValidator);
		setComparator(fSorter);
		addFilter(fFilter);
		setTitle(Messages.getString("SourceFolderSelectionDialog.0"));
		setMessage(Messages.getString("SourceFolderSelectionDialog.1"));
	}

	private static ITreeContentProvider createContentProvider() {
		return new CElementContentProvider();
	}

	private static ILabelProvider createLabelProvider() {
		return new CElementLabelProvider(CElementLabelProvider.SHOW_DEFAULT);
	}
}
