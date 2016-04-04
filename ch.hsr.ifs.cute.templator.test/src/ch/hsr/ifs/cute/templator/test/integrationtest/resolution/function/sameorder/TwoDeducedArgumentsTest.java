package ch.hsr.ifs.cute.templator.test.integrationtest.resolution.function.sameorder;

import org.junit.Test;

import ch.hsr.ifs.cute.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.cute.templator.test.FunctionTemplateResolutionTest;

public class TwoDeducedArgumentsTest extends FunctionTemplateResolutionTest {
    @Test
    public void testOuterArgumentMapIsIntChar() {
        testOuterArgumentMap(INT, CHAR);
    }

    @Test
    public void testSubcallArgumentMapIsIntChar() throws TemplatorException {
        testFirstInnerArgumentMap(INT, CHAR);
    }

    @Test
    public void testSubcallResolvedToFunctionTemplateAndNotNormalFunction() throws TemplatorException {
        testFirstInnerCallResolvesToFirstDefinition();
    }
}
