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
