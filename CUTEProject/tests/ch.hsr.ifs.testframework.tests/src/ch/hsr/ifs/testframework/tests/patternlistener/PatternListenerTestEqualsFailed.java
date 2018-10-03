/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.tests.patternlistener;

import java.io.IOException;

import org.eclipse.jface.text.IRegion;

import ch.hsr.ifs.testframework.launch.ConsolePatternListener;
import ch.hsr.ifs.testframework.tests.PatternListenerBase;
import ch.hsr.ifs.testframework.tests.mock.DummyTestEventHandler;


/**
 * @author Emanuel Graf
 *
 */
public class PatternListenerTestEqualsFailed extends PatternListenerBase {

    private static final String TEST_NAME_EXP      = "xUnitTest";
    private static final String MSG_EXP            = "evaluated: `Factorial(0)`, expected: <3> but was: <1>";
    private static final Object TEST_FILE_NAME_EXP = "../src/sample1_unittest.cc";
    private static final int    LINE_NO_EXP        = 104;

    private String testNameStart;
    private String testNameEnd;
    private String msg;
    private String testFileName;
    private int    lineNr;

    final class TestFailedHandler extends DummyTestEventHandler {

        @Override
        protected void handleFailure(IRegion reg, String testName, String fileName, String lineNo, String reason) {
            testNameEnd = testName;
            testFileName = fileName;
            lineNr = Integer.parseInt(lineNo);
            msg = reason;
        }

        @Override
        protected void handleTestStart(IRegion reg, String testname) {
            testNameStart = testname;
        }
    }

    public void testListenerEvents() throws IOException, InterruptedException {
        emulateTestRun();
        assertEquals("Teststart name", TEST_NAME_EXP, testNameStart);
        assertEquals("Testend name", TEST_NAME_EXP, testNameEnd);
        assertEquals("Message", MSG_EXP, msg);
        assertEquals("Filename", TEST_FILE_NAME_EXP, testFileName);
        assertEquals("Line", LINE_NO_EXP, lineNr);
    }

    @Override
    protected void addTestEventHandler(ConsolePatternListener lis) {
        lis.addHandler(new TestFailedHandler());
    }

    @Override
    protected String getInputFileName() {
        return "failedEqualsTest.txt";
    }
}
