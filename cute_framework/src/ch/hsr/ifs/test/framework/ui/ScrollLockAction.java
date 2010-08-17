/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule für Technik  
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
