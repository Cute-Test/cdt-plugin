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
package ch.hsr.ifs.cute.core.ui;

import org.eclipse.jface.action.Action;

import ch.hsr.ifs.cute.core.CuteCorePlugin;

/**
 * @author Emanuel Graf
 *
 */
public class ShowPreviousFailureAction extends Action {
	
	private TestRunnerViewPart trViewPart;

	public ShowPreviousFailureAction(TestRunnerViewPart trViewPart) {
		super("Previous Failure");
		setDisabledImageDescriptor(CuteCorePlugin.getImageDescriptor("dlcl16/select_prev.gif")); //$NON-NLS-1$
		setHoverImageDescriptor(CuteCorePlugin.getImageDescriptor("obj16/select_prev.gif")); //$NON-NLS-1$
		setImageDescriptor(CuteCorePlugin.getImageDescriptor("obj16/select_prev.gif")); //$NON-NLS-1$
		setToolTipText("Show Previous Failed Test"); 
		this.trViewPart = trViewPart;
	}

	@Override
	public void run() {
		trViewPart.selectPrevFailure();
	}
	
	

}
