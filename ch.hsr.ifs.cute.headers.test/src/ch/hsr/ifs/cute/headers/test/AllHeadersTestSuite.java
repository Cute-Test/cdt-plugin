/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.headers.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 */
public class AllHeadersTestSuite extends TestSuite {

	public AllHeadersTestSuite() {
		super("All Header Tests");
		addTestSuite(CopyHeaders2_1Test.class);
		addTestSuite(CopyHeaders2_0Test.class);
		addTestSuite(CopyHeaders1_7Test.class);
	}

	public static Test suite() {
		return new AllHeadersTestSuite();
	}
}
