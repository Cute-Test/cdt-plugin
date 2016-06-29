package ch.hsr.ifs.cute.templator.test.integrationtest.resolution.function;

import org.junit.Test;

import ch.hsr.ifs.cute.templator.test.FunctionTemplateResolutionTest;

public class OneLevelWithOneTemplateIdArgumentTest extends FunctionTemplateResolutionTest {
    @Test
    public void testNumberOfSubcallsIsZero() {
        assertEquals(0, firstStatementInMain.getSubNames().size());
    }

    @Test
    public void testArgumentMapIsDouble() {
        testOuterArgumentMap(DOUBLE);
    }
}
