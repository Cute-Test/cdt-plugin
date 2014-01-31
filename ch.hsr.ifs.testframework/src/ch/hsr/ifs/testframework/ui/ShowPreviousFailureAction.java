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

	private final TestRunnerViewPart trViewPart;
	private static Messages msg = TestFrameworkPlugin.getMessages();

	public ShowPreviousFailureAction(TestRunnerViewPart trViewPart) {
		super(msg.getString("ShowPreviousFailureAction.ShowPrevFailedTest"));
		setDisabledImageDescriptor(TestFrameworkPlugin.getImageDescriptor("dlcl16/select_prev.gif"));
		setHoverImageDescriptor(TestFrameworkPlugin.getImageDescriptor("obj16/select_prev.gif"));
		setImageDescriptor(TestFrameworkPlugin.getImageDescriptor("obj16/select_prev.gif"));
		setToolTipText(msg.getString("ShowPreviousFailureAction.ShowPrevFailedTest"));
		this.trViewPart = trViewPart;
	}

	@Override
	public void run() {
		trViewPart.selectPrevFailure();
	}

}
