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
package ch.hsr.ifs.cute.framework.ui;

import org.eclipse.jface.action.Action;

import ch.hsr.ifs.cute.framework.CuteFrameworkPlugin;
import ch.hsr.ifs.cute.framework.preference.PreferenceConstants;

/**
 * @author Emanuel Graf
 *
 */
public class ShowWhiteSpaceAction extends Action {

	private CuteTextMergeViewer viewer;
	
	public ShowWhiteSpaceAction(CuteTextMergeViewer compareViewer) {
		super("Show Whitespace Characters", AS_CHECK_BOX); 
		viewer = compareViewer;
		setImageDescriptor(CuteFrameworkPlugin.getImageDescriptor("dlcl16/show_whitespace_chars.gif")); //$NON-NLS-1$
		setToolTipText("Show Whitespace Characters");
		setChecked(CuteFrameworkPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.SHOW_WHITESPACES));
	}

	@Override
	public void run() {
		boolean show = ! CuteFrameworkPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.SHOW_WHITESPACES);
		CuteFrameworkPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.SHOW_WHITESPACES, show);
		viewer.showWhitespaces(show);
	}

}
