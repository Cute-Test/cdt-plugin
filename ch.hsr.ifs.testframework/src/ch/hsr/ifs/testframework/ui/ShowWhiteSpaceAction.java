/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.ui;

import org.eclipse.jface.action.Action;

import ch.hsr.ifs.testframework.Messages;
import ch.hsr.ifs.testframework.TestFrameworkPlugin;
import ch.hsr.ifs.testframework.preference.PreferenceConstants;

/**
 * @author Emanuel Graf
 *
 */
public class ShowWhiteSpaceAction extends Action {

	private final CuteTextMergeViewer viewer;
	private static Messages msg = TestFrameworkPlugin.getMessages();
	
	public ShowWhiteSpaceAction(CuteTextMergeViewer compareViewer) {
		super(msg.getString("ShowWhiteSpaceAction.ShowWhitespaceChar"), AS_CHECK_BOX);
		viewer = compareViewer;
		setImageDescriptor(TestFrameworkPlugin.getImageDescriptor("dlcl16/show_whitespace_chars.gif"));
		setToolTipText(msg.getString("ShowWhiteSpaceAction.ShowWhitespaceChar"));
		setChecked(TestFrameworkPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.SHOW_WHITESPACES));
	}

	@Override
	public void run() {
		boolean show = !TestFrameworkPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.SHOW_WHITESPACES);
		TestFrameworkPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.SHOW_WHITESPACES, show);
		viewer.showWhitespaces(show);
	}
}
