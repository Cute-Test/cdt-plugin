package ch.hsr.ifs.testframework.test.patternlistener;

import junit.framework.TestCase;
import ch.hsr.ifs.testframework.model.TestFailure;

public class TestFailureBackslashEscaped extends TestCase {
    
    
    //    ((.*)(\t)(.*)(\t)(.*)(\t)(.*)(\t))
    
    public void testFailureCurlyBraced2Bug() throws Exception {
        TestFailure testFailure = new TestFailure("Ring5{3} == Ring{2} expected:\tRing5{3}\tbut was:\tRing5{2}\t");
        assertEquals("Ring5{3}", testFailure.getExpected());
        assertEquals("Ring5{2}", testFailure.getWas());
    }
    
    public void testFailureBackslashEscapedInExpectedAndWas() throws Exception {
        TestFailure testFailure = new TestFailure("backslash == asdf expected:\t\\\\\tbut was:\tasdf\t");
        assertEquals("\\", testFailure.getExpected());
        assertEquals("asdf", testFailure.getWas());
    }
    
    public void testFailureBackslashEscapedInMessage() throws Exception { 
        TestFailure testFailure = new TestFailure("backslash == asdf expected:\t\\\\\tbut was:\tasdf\t");
        assertEquals("backslash == asdf expected: \\ but was: asdf", testFailure.getMsg());
    }
}
