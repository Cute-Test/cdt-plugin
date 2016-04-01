package ch.hsr.ifs.templator.test.integrationtest.resolution.function.sameorder;

import org.junit.Test;

import ch.hsr.ifs.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.templator.test.FunctionTemplateResolutionTest;

public class CompleteDefaultArgumentsAndEmptyTemplateIdWithoutArgumentsTest extends FunctionTemplateResolutionTest {

    @Test
    public void testOuterArgumentMapIsIntBool() {
        testOuterArgumentMap(INT, BOOL);
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
