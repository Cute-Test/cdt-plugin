package ch.hsr.ifs.templator.test.integrationtest.resolution.function;

import org.junit.Test;

import ch.hsr.ifs.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.templator.test.FunctionTemplateResolutionTest;

public class ReferenceTest extends FunctionTemplateResolutionTest {

    @Test
    public void testOuterArgumentMapIsDoubleUnsignedLongLong() {
        testOuterArgumentMap(DOUBLE, UNSIGNED_LONG_LONG);
    }

    @Test
    public void testSubcallArgumentMapIsDoubleIntReferenceIntReference() throws TemplatorException {
        testFirstInnerArgumentMap(DOUBLE, INT, INT);
    }

    @Test
    public void testSubcallResolvedToFunctionTemplateAndNotNormalFunction() throws TemplatorException {
        testFirstInnerCallResolvesToFirstDefinition();
    }
}
