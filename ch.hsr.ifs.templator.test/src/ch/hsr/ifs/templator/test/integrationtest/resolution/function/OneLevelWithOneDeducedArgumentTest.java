package ch.hsr.ifs.templator.test.integrationtest.resolution.function;

import org.junit.Test;

import ch.hsr.ifs.templator.test.FunctionTemplateResolutionTest;

public class OneLevelWithOneDeducedArgumentTest extends FunctionTemplateResolutionTest {
    @Test
    public void testNumberOfSubcallsIsZero() {
        assertEquals(0, firstStatementInMain.getSubNames().size());
    }

    @Test
    public void testArgumentMapIsInt() {
        testOuterArgumentMap(INT);
    }
}
