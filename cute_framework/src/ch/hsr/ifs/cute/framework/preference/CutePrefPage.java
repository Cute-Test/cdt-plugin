/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Emanuel Graf - initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cute.framework.preference;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.hsr.ifs.cute.framework.CuteFrameworkPlugin;
import ch.hsr.ifs.cute.framework.Messages;

/**
 * @author Emanuel Graf
 *
 */
public class CutePrefPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	

	public CutePrefPage() {
		super(GRID);
		setPreferenceStore(CuteFrameworkPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.getString("CutePrefPage.CutePrefPage")); //$NON-NLS-1$
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(PreferenceConstants.SHOW_WHITESPACES, Messages.getString("CutePrefPage.ShowWhitespace"), getFieldEditorParent())); //$NON-NLS-1$

	}

	public void init(IWorkbench workbench) {
		// Do nothing		
	}

	@Override
	public Image getImage() {
		return CuteFrameworkPlugin.getImageDescriptor("obj16/cute_app.gif").createImage(); //$NON-NLS-1$
	}
	
	

}
