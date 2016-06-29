package ch.hsr.ifs.cute.templator.test.integrationtest.resolution.function.sameorder;

import org.junit.Test;

import ch.hsr.ifs.cute.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.cute.templator.test.FunctionTemplateResolutionTest;

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
