/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.headers.v10.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Emanuel Graf IFS
 *
 */
public class Headers1_0Suite extends TestSuite {
	
	public Headers1_0Suite() {
		super("All Headers 1.0 Tests"); //$NON-NLS-1$
		addTestSuite(CopyHeadersTest.class);
	}

	public static Test suite() {
		return new Headers1_0Suite();
	}


}
