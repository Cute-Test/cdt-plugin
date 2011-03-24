/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.test.framework.ui;

import org.eclipse.jface.action.Action;

import ch.hsr.ifs.test.framework.Messages;
import ch.hsr.ifs.test.framework.TestFrameworkPlugin;

/**
 * @author Emanuel Graf
 *
 */
public class ScrollLockAction extends Action {
	
	private TestRunnerViewPart view;
	private static Messages msg = TestFrameworkPlugin.getMessages();
	
	public ScrollLockAction(TestRunnerViewPart view) {
		super(msg.getString("ScrollLockAction.ScrollLock")); //$NON-NLS-1$
		this.view = view;
		setToolTipText(msg.getString("ScrollLockAction.ScrollLock"));  //$NON-NLS-1$
		setDisabledImageDescriptor(TestFrameworkPlugin.getImageDescriptor("dlcl16/lock.gif")); //$NON-NLS-1$
		setHoverImageDescriptor(TestFrameworkPlugin.getImageDescriptor("obj16/lock.gif")); //$NON-NLS-1$
		setImageDescriptor(TestFrameworkPlugin.getImageDescriptor("obj16/lock.gif")); //$NON-NLS-1$
		setChecked(false);		
	}

	@Override
	public void run() {
		view.setAutoScroll(!isChecked());
	}
	
	

}
