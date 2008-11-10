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
package ch.hsr.ifs.test.framework.ui;

import org.eclipse.jface.action.Action;

import ch.hsr.ifs.test.framework.CuteFrameworkPlugin;
import ch.hsr.ifs.test.framework.Messages;
import ch.hsr.ifs.test.framework.preference.PreferenceConstants;

/**
 * @author Emanuel Graf
 *
 */
public class ShowWhiteSpaceAction extends Action {

	private CuteTextMergeViewer viewer;
	private static Messages msg = CuteFrameworkPlugin.getMessages();
	
	public ShowWhiteSpaceAction(CuteTextMergeViewer compareViewer) {
		super(msg.getString("ShowWhiteSpaceAction.ShowWhitespaceChar"), AS_CHECK_BOX);  //$NON-NLS-1$
		viewer = compareViewer;
		setImageDescriptor(CuteFrameworkPlugin.getImageDescriptor("dlcl16/show_whitespace_chars.gif")); //$NON-NLS-1$
		setToolTipText(msg.getString("ShowWhiteSpaceAction.ShowWhitespaceChar")); //$NON-NLS-1$
		setChecked(CuteFrameworkPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.SHOW_WHITESPACES));
	}

	@Override
	public void run() {
		boolean show = ! CuteFrameworkPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.SHOW_WHITESPACES);
		CuteFrameworkPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.SHOW_WHITESPACES, show);
		viewer.showWhitespaces(show);
	}

}
