/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.ui;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;

import ch.hsr.ifs.testframework.model.TestElement;

/**
 * @author egraf
 * 
 */
public class CuteTestSelecetionListener implements ISelectionChangedListener {

	private final TestViewer viewer;

	public CuteTestSelecetionListener(TestViewer viewer) {
		super();
		this.viewer = viewer;
	}

	public void selectionChanged(SelectionChangedEvent event) {
		if (event.getSelection() instanceof TreeSelection) {
			TreeSelection treeSel = (TreeSelection) event.getSelection();
			if (treeSel.getFirstElement() instanceof TestElement) {
				TestElement testElement = (TestElement) treeSel.getFirstElement();
				viewer.showTestDetails(testElement);
			}
		}
	}

}
