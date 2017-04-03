/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import ch.hsr.ifs.cute.gcov.test.job.DeleteMarkersTest;
import ch.hsr.ifs.cute.gcov.test.parser.ModelBuilderLineParserTest;

/**
 * @author Emanuel Graf IFS
 * 
 */
public class TestSuiteAll extends TestSuite {

	public TestSuiteAll() {
		super("All Gcov Tests");
		addTestSuite(ModelBuilderLineParserTest.class);
		addTestSuite(DeleteMarkersTest.class);
	}

	public static Test suite() {
		return new TestSuiteAll();
	}

}
