/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.headers.v17.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 */
public class Headers1_7Suite extends TestSuite {
	
	public Headers1_7Suite() {
		super("All Headers 1.7 Tests"); //$NON-NLS-1$
		addTestSuite(CopyHeadersTest.class);
	}

	public static Test suite() {
		return new Headers1_7Suite();
	}


}
