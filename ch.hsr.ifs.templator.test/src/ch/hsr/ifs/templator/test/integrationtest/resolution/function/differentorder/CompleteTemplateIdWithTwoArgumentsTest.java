package ch.hsr.ifs.templator.test.integrationtest.resolution.function.differentorder;

import org.junit.Test;

import ch.hsr.ifs.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.templator.test.FunctionTemplateResolutionTest;

public class CompleteTemplateIdWithTwoArgumentsTest extends FunctionTemplateResolutionTest {

    @Test
    public void testOuterArgumentMapIsIntInt() {
        testOuterArgumentMap(INT, INT);
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
