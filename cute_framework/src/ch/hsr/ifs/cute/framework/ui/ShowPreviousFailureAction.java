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
package ch.hsr.ifs.cute.framework.ui;

import org.eclipse.jface.action.Action;

import ch.hsr.ifs.cute.framework.CuteFrameworkPlugin;
import ch.hsr.ifs.cute.framework.Messages;

/**
 * @author Emanuel Graf
 *
 */
public class ShowPreviousFailureAction extends Action {
	
	private TestRunnerViewPart trViewPart;

	public ShowPreviousFailureAction(TestRunnerViewPart trViewPart) {
		super(Messages.getString("ShowPreviousFailureAction.ShowPreviousFaildTest")); //$NON-NLS-1$
		setDisabledImageDescriptor(CuteFrameworkPlugin.getImageDescriptor("dlcl16/select_prev.gif")); //$NON-NLS-1$
		setHoverImageDescriptor(CuteFrameworkPlugin.getImageDescriptor("obj16/select_prev.gif")); //$NON-NLS-1$
		setImageDescriptor(CuteFrameworkPlugin.getImageDescriptor("obj16/select_prev.gif")); //$NON-NLS-1$
		setToolTipText(Messages.getString("ShowPreviousFailureAction.ShowPreviousFaildTest"));  //$NON-NLS-1$
		this.trViewPart = trViewPart;
	}

	@Override
	public void run() {
		trViewPart.selectPrevFailure();
	}
	
	

}
