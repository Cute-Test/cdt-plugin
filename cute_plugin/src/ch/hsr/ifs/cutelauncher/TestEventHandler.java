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
package ch.hsr.ifs.cutelauncher;

import org.eclipse.jface.text.IRegion;

public interface TestEventHandler {

	public abstract void handleError(IRegion reg, String[] parts);

	public abstract void handleSuccess(IRegion reg, String[] parts);

	public abstract void handleEnding(IRegion reg, String[] parts);

	public abstract void handleBeginning(IRegion reg, String[] parts);
	
	public abstract void handleFailure(IRegion reg, String[] parts);
	
	public abstract void handleTestStart(IRegion reg, String[] parts);

}