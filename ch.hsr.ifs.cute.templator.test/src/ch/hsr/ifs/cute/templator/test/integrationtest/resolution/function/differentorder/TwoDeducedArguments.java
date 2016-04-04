package ch.hsr.ifs.cute.templator.test.integrationtest.resolution.function.differentorder;

import org.junit.Test;

import ch.hsr.ifs.cute.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.cute.templator.test.FunctionTemplateResolutionTest;

public class TwoDeducedArguments extends FunctionTemplateResolutionTest {
    @Test
    public void testOuterArgumentMapIsIntChar() {
        testOuterArgumentMap(INT, CHAR);
    }

    @Test
    public void testSubcallArgumentMapIsCharInt() throws TemplatorException {
        testFirstInnerArgumentMap(CHAR, INT);
    }

    @Test
    public void testSubcallResolvedToFunctionTemplateAndNotNormalFunction() throws TemplatorException {
        testFirstInnerCallResolvesToFirstDefinition();
    }
}
