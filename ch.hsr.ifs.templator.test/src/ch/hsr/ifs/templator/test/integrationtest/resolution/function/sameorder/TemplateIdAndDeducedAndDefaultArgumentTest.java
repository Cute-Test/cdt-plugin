package ch.hsr.ifs.templator.test.integrationtest.resolution.function.sameorder;

import org.junit.Test;

import ch.hsr.ifs.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.templator.test.FunctionTemplateResolutionTest;

public class TemplateIdAndDeducedAndDefaultArgumentTest extends FunctionTemplateResolutionTest {

    @Test
    public void testOuterArgumentMapIsBoolUnsignedLongLong() {
        testOuterArgumentMap(BOOL, UNSIGNED_LONG_LONG);
    }

    @Test
    public void testSubcallArgumentMapIsDoubleBoolInt() throws TemplatorException {
        testFirstInnerArgumentMap(DOUBLE, BOOL, INT);
    }

    @Test
    public void testSubcallResolvedToFunctionTemplateAndNotNormalFunction() throws TemplatorException {
        testFirstInnerCallResolvesToFirstDefinition();
    }
}
