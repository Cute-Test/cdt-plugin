/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule f�r Technik  
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

public class TestSession{
	private TestSuite root;

	public TestSession(TestSuite root) {
		super();
		this.root = root;
	}

	public TestSuite getRoot() {
		return root;
	}
	
	
}