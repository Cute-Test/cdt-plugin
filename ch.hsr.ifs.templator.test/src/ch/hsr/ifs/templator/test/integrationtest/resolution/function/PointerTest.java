package ch.hsr.ifs.templator.test.integrationtest.resolution.function;

import org.junit.Test;

import ch.hsr.ifs.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.templator.test.FunctionTemplateResolutionTest;

public class PointerTest extends FunctionTemplateResolutionTest {

    @Test
    public void testOuterArgumentMapIsDoubleUnsignedLongLong() {
        testOuterArgumentMap(DOUBLE, UNSIGNED_LONG_LONG);
    }

    @Test
    public void testSubcallArgumentMapIsDoubleIntPointerIntPointer() throws TemplatorException {
        testFirstInnerArgumentMap(DOUBLE, INT_POINTER, INT_POINTER);
    }

    @Test
    public void testSubcallResolvedToFunctionTemplateAndNotNormalFunction() throws TemplatorException {
        testFirstInnerCallResolvesToFirstDefinition();
    }
}
