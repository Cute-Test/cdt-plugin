package ch.hsr.ifs.templator.test.integrationtest.resolution.function;

import org.junit.Test;

import ch.hsr.ifs.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.templator.test.FunctionTemplateResolutionTest;

public class PassNormalFunctionsAsArgumentsTest extends FunctionTemplateResolutionTest {

    @Test
    public void testOuterArgumentMapIsInt() {
        testOuterArgumentMap(INT);
    }

    @Test
    public void testSubcallArgumentMapIsDoubleInt() throws TemplatorException {
        testFirstInnerArgumentMap(DOUBLE, INT);
    }

    @Test
    public void testSubcallResolvedToFunctionTemplateAndNotNormalFunction() throws TemplatorException {
        testFirstInnerCallResolvesToFirstDefinition();
    }
}
