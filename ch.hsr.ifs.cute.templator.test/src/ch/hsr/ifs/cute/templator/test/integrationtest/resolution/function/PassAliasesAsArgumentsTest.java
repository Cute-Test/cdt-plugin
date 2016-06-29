package ch.hsr.ifs.cute.templator.test.integrationtest.resolution.function;

import org.junit.Test;

import ch.hsr.ifs.cute.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.cute.templator.test.FunctionTemplateResolutionTest;

public class PassAliasesAsArgumentsTest extends FunctionTemplateResolutionTest {

    @Test
    public void testOuterArgumentMapIsInt() {
        testOuterArgumentMap(INT);
    }

    @Test
    public void testSubcallArgumentMapIsUnsignedLongLong() throws TemplatorException {
        testFirstInnerArgumentMap(UNSIGNED_LONG, LONG);
    }

    @Test
    public void testSubcallResolvedToFunctionTemplateAndNotNormalFunction() throws TemplatorException {
        testFirstInnerCallResolvesToFirstDefinition();
    }
}
