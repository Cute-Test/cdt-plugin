package ch.hsr.ifs.cute.templator.test.integrationtest.resolution.function;

import org.junit.Test;

import ch.hsr.ifs.cute.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.cute.templator.test.FunctionTemplateResolutionTest;

public class PassVariablesAsArgumentsTest extends FunctionTemplateResolutionTest {

    @Test
    public void testOuterArgumentMapIsChar() {
        testOuterArgumentMap(CHAR);
    }

    @Test
    public void testSubcallArgumentMapIsDoubleIntChar() throws TemplatorException {
        testFirstInnerArgumentMap(DOUBLE, INT, CHAR);
    }

    @Test
    public void testSubcallResolvedToFunctionTemplateAndNotNormalFunction() throws TemplatorException {
        testFirstInnerCallResolvesToFirstDefinition();
    }
}
