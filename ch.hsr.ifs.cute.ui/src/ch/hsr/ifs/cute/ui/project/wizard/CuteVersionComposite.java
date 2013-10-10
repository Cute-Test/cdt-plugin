/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.wizard;

import java.util.SortedSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.hsr.ifs.cute.ui.UiPlugin;
import ch.hsr.ifs.cute.ui.project.headers.ICuteHeaders;

/**
 * @author egraf
 * @since 4.0
 * 
 */
public class CuteVersionComposite extends Composite {

	private Combo combo;

	public CuteVersionComposite(Composite parent, String currentCuteHeadersVersionName) {
		super(parent, SWT.NULL);
		createCuteVersionCompsite(parent, currentCuteHeadersVersionName);
	}

	public CuteVersionComposite(Composite parent) {
		this(parent, null);
	}

	public String getVersionString() {
		return combo.getText();
	}

	public String getErrorMessage() {
		if (combo.getItems().length == 0) {
			return Messages.getString("CuteVersionComposite.NoCuteInstalled"); //$NON-NLS-1$
		}
		return null;
	}

	public boolean isComplete() {
		return !combo.getText().isEmpty(); //$NON-NLS-1$
	}

	private void createCuteVersionCompsite(Composite parent, String currentCuteHeadersVersionName) {
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);
		setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(this, SWT.HORIZONTAL);
		label.setText(Messages.getString("CuteVersionComposite.CuteVersion")); //$NON-NLS-1$

		int indexToSelect = 0;
		int i = 0;
		combo = new Combo(this, SWT.READ_ONLY | SWT.DROP_DOWN);
		SortedSet<ICuteHeaders> set = UiPlugin.getInstalledCuteHeaders();
		if (!set.isEmpty()) {
			for (ICuteHeaders cuteHeaders : set) {
				String versionString = cuteHeaders.getVersionString();
				combo.add(versionString);
				if (versionString.equals(currentCuteHeadersVersionName)) {
					indexToSelect = i;
				}
				i++;
			}
			combo.setText(combo.getItem(0));
		}
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		combo.select(indexToSelect);
		combo.setLayoutData(data);
	}

}
