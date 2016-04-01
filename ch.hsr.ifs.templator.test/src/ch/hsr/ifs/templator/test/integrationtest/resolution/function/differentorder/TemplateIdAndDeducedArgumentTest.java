package ch.hsr.ifs.templator.test.integrationtest.resolution.function.differentorder;

import org.junit.Test;

import ch.hsr.ifs.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.templator.test.FunctionTemplateResolutionTest;

public class TemplateIdAndDeducedArgumentTest extends FunctionTemplateResolutionTest {

    @Test
    public void testOuterArgumentMapIsDoubleInt() {
        testOuterArgumentMap(DOUBLE, INT);
    }

    @Test
    public void testSubcallArgumentMapIsDoubleDouble() throws TemplatorException {
        testFirstInnerArgumentMap(DOUBLE, DOUBLE);
    }

    @Test
    public void testSubcallResolvedToFunctionTemplateAndNotNormalFunction() throws TemplatorException {
        testFirstInnerCallResolvesToFirstDefinition();
    }
}
