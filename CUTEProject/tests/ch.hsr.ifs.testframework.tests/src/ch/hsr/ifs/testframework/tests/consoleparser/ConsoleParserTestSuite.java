/*******************************************************************************
 * Copyright (c) 2018, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/

package ch.hsr.ifs.testframework.tests.consoleparser;

import junit.framework.Test;
import junit.framework.TestSuite;


public class ConsoleParserTestSuite {

    public static Test suite() {

        TestSuite suite = new TestSuite("Console Parser Test Suite");
        suite.addTestSuite(ConsoleParserFailureTest.class);
        return suite;

    }

}
