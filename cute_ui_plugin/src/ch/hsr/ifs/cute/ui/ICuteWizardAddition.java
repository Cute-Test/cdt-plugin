/*******************************************************************************
 * Copyright (c) 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cute.ui;

import org.eclipse.swt.widgets.Composite;

/**
 * @author Emanuel Graf IFS
 *
 */
public interface ICuteWizardAddition {
	
	public void createComposite(Composite comp);
	
	public ICuteWizardAdditionHandler getHandler();

}
