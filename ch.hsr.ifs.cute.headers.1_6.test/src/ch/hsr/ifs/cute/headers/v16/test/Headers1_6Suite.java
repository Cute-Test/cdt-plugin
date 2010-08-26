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
package ch.hsr.ifs.cute.headers.v16.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Emanuel Graf IFS
 *
 */
public class Headers1_6Suite extends TestSuite {
	
	public Headers1_6Suite() {
		super("All Headers 1.5 Tests"); //$NON-NLS-1$
		addTestSuite(CopyHeadersTest.class);
	}

	public static Test suite() {
		return new Headers1_6Suite();
	}


}
