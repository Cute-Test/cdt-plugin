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
package ch.hsr.ifs.test.framework.preference;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.hsr.ifs.test.framework.TestFrameworkPlugin;
import ch.hsr.ifs.test.framework.ImageProvider;
import ch.hsr.ifs.test.framework.Messages;

/**
 * @author Emanuel Graf
 *
 */
public class CutePrefPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	private Messages msg = TestFrameworkPlugin.getMessages();

	public CutePrefPage() {
		super(GRID);
		setPreferenceStore(TestFrameworkPlugin.getDefault().getPreferenceStore());
		setDescription(msg.getString("CutePrefPage.CuteRefPage")); //$NON-NLS-1$
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(PreferenceConstants.SHOW_WHITESPACES, msg.getString("CutePrefPage.ShowWhiteSpaces"), getFieldEditorParent())); //$NON-NLS-1$

	}

	public void init(IWorkbench workbench) {
	}

	@Override
	public Image getImage() {
		return TestFrameworkPlugin.getImageProvider().getImage(ImageProvider.APP_LOGO).createImage();
	}
	
	

}
