package ch.hsr.ifs.templator.test.integrationtest.resolution.function.sameorder;

import org.junit.Test;

import ch.hsr.ifs.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.templator.test.FunctionTemplateResolutionTest;

public class CompleteDefaultArgumentsAndEmptyTemplateIdWithArgumentsTest extends FunctionTemplateResolutionTest {
    @Test
    public void testOuterArgumentMapIsIntBool() {
        testOuterArgumentMap(INT, BOOL);
    }

    @Test
    public void testSubcallArgumentMapIsIntBool() throws TemplatorException {
        testFirstInnerArgumentMap(INT, BOOL);
    }

    @Test
    public void testSubcallResolvedToFunctionTemplateAndNotNormalFunction() throws TemplatorException {
        testFirstInnerCallResolvesToFirstDefinition();
    }

}
