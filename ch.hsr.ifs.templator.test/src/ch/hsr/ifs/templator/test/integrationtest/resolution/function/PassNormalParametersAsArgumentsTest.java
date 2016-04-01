package ch.hsr.ifs.templator.test.integrationtest.resolution.function;

import org.junit.Test;

import ch.hsr.ifs.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.templator.test.FunctionTemplateResolutionTest;

public class PassNormalParametersAsArgumentsTest extends FunctionTemplateResolutionTest {

    @Test
    public void testOuterArgumentMapIsDouble() {
        testOuterArgumentMap(DOUBLE);
    }

    @Test
    public void testSubcallArgumentMapIsDoubleCharInt() throws TemplatorException {
        testFirstInnerArgumentMap(DOUBLE, CHAR, INT);
    }

    @Test
    public void testSubcallResolvedToFunctionTemplateAndNotNormalFunction() throws TemplatorException {
        testFirstInnerCallResolvesToFirstDefinition();
    }
}
