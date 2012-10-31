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

/**
 * @author Emanuel Graf
 *
 */
public class ShowPreviousFailureAction extends Action {
	
	private TestRunnerViewPart trViewPart;
	private static Messages msg = TestFrameworkPlugin.getMessages();

	public ShowPreviousFailureAction(TestRunnerViewPart trViewPart) {
		super(msg.getString("ShowPreviousFailureAction.ShowPrevFailedTest")); //$NON-NLS-1$
		setDisabledImageDescriptor(TestFrameworkPlugin.getImageDescriptor("dlcl16/select_prev.gif")); //$NON-NLS-1$
		setHoverImageDescriptor(TestFrameworkPlugin.getImageDescriptor("obj16/select_prev.gif")); //$NON-NLS-1$
		setImageDescriptor(TestFrameworkPlugin.getImageDescriptor("obj16/select_prev.gif")); //$NON-NLS-1$
		setToolTipText(msg.getString("ShowPreviousFailureAction.ShowPrevFailedTest"));  //$NON-NLS-1$
		this.trViewPart = trViewPart;
	}

	@Override
	public void run() {
		trViewPart.selectPrevFailure();
	}
	
	

}
