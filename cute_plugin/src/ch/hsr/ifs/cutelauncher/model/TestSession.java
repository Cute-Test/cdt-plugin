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
package ch.hsr.ifs.cutelauncher.model;

import org.eclipse.debug.core.ILaunch;

public class TestSession{
	private TestSuite root;
	
	private ILaunch launch;

	public TestSession(TestSuite root, ILaunch launch) {
		super();
		this.root = root;
		this.launch = launch;
	}

	public TestSuite getRoot() {
		return root;
	}

	public ILaunch getLaunch() {
		return launch;
	}
	
}