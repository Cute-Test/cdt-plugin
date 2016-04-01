package ch.hsr.ifs.templator.test.integrationtest.resolution.function;

import org.junit.Test;

import ch.hsr.ifs.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.templator.test.FunctionTemplateResolutionTest;

public class GeneralWithOneLevelTest extends FunctionTemplateResolutionTest {

    @Test
    public void testNumberOfSubcalls() throws TemplatorException {
        firstStatementInMain.searchSubNames(loadingProgress);
        assertEquals(0, firstStatementInMain.getSubNames().size());
    }

    @Test
    public void testNumberOfResolvedSubcallsStaysZero() throws TemplatorException {
        int numberOfResolvedSubcallsBefore = getOnlyFunctionCallSubstatements(firstStatementInMain).size();
        assertEquals(0, numberOfResolvedSubcallsBefore);

        firstStatementInMain.searchSubNames(loadingProgress);

        int numberOfResolvedSubcallsAfter = firstStatementInMain.getSubNames().size();
        assertEquals(0, numberOfResolvedSubcallsAfter);
    }

    public void testResolvedSubcallsNotNull() {
        assertTrue(firstStatementInMain.getSubNames() != null);
    }

}
