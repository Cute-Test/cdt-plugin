/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.tests.modelbuilder;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Emanuel Graf IFS
 *
 */
public class ModelBuilderSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for ch.hsr.ifs.cutelauncher.test.modelBuilderTests");
        suite.addTest(ModelBuilderTest.suite("sessionTest.txt"));
        suite.addTest(ModelBuilderTest.suite("suiteTest.txt"));
        suite.addTest(ModelBuilderTest.suite("suiteTest2.txt"));
        suite.addTest(ModelBuilderTest.suite("suiteTest3.txt"));
        suite.addTest(ModelBuilderTest.suite("suiteTest4.txt"));
        suite.addTest(ModelBuilderTest.suite("suiteTest5.txt"));
        suite.addTest(ModelBuilderTest.suite("suiteTest6.txt"));
        suite.addTest(ModelBuilderTest.suite("failedTest.txt"));
        suite.addTest(ModelBuilderTest.suite("failedEqualsTest.txt"));
        suite.addTest(ModelBuilderTest.suite("errorTest.txt"));
        suite.addTest(ModelBuilderTest.suite("successTest.txt"));
        suite.addTest(ModelBuilderTest.suite("outputInTest.txt"));
        return suite;
    }

}
