/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.test.framework.ui;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.hsr.ifs.test.framework.model.ITestComposite;
import ch.hsr.ifs.test.framework.model.TestElement;

/**
 * @author egraf
 *
 */
public class CuteTestTreeContentProvieder implements ITreeContentProvider {

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ITestComposite) {
			ITestComposite composite = (ITestComposite) parentElement;
			return composite.getElements().toArray();
		}
		return null;
	}

	public Object getParent(Object element) {
		if (element instanceof TestElement) {
			TestElement tElement = (TestElement) element;
			return tElement.getParent();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof ITestComposite) {
			ITestComposite composite = (ITestComposite) element;
			boolean ret = composite.getElements().size() > 0;
			return ret;
		}
		return false;
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof ITestComposite) {
			ITestComposite composite = (ITestComposite) inputElement;
			return composite.getElements().toArray();
		}
		return null;
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}