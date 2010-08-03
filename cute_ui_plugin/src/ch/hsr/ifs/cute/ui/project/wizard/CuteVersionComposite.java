/*******************************************************************************
 * Copyright (c) 2009 Institute for Software, HSR Hochschule für Technik  
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Emanuel Graf - initial API and implementation 
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

	/**
	 * @param parent
	 * @param style
	 */
	public CuteVersionComposite(Composite parent) {
		super(parent, SWT.NULL);
		createCuteVersionCompsite(parent);
	}
	
	public String getVersionString() {
		return combo.getText();
	}
	
	public String getErrorMessage() {
		if(combo.getItems().length == 0) {
			return Messages.getString("CuteVersionComposite.NoCuteInstalled"); //$NON-NLS-1$
		}
		return null;
	}
	
	public boolean isComplete() {
		return !combo.getText().equals(""); //$NON-NLS-1$
	}
	
	private void createCuteVersionCompsite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		
		Label label = new Label(composite, SWT.HORIZONTAL);
		label.setText(Messages.getString("CuteVersionComposite.CuteVersion")); //$NON-NLS-1$
		
		combo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN);
		SortedSet<ICuteHeaders> set = UiPlugin.getInstalledCuteHeaders();
		if(!set.isEmpty()) {
			for (ICuteHeaders cuteHeaders : set) {
				combo.add(cuteHeaders.getVersionString());
			}
			combo.setText(combo.getItem(0));
		}
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		combo.setLayoutData(data);
	}

}
