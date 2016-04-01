package ch.hsr.ifs.templator.test.integrationtest.resolution.function.sameorder;

import org.junit.Test;

import ch.hsr.ifs.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.templator.test.FunctionTemplateResolutionTest;

public class TemplateIdAndDefaultArgumentTest extends FunctionTemplateResolutionTest {
    @Test
    public void testOuterArgumentMapIsIntLong() {
        testOuterArgumentMap(INT, LONG);
    }

    @Test
    public void testSubcallArgumentMapIsIntInt() throws TemplatorException {
        testFirstInnerArgumentMap(INT, INT);
    }

    @Test
    public void testSubcallResolvedToFunctionTemplateAndNotNormalFunction() throws TemplatorException {
        testFirstInnerCallResolvesToFirstDefinition();
    }

}
