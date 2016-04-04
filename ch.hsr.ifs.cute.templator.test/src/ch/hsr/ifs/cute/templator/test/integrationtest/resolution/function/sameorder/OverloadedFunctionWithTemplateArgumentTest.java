package ch.hsr.ifs.cute.templator.test.integrationtest.resolution.function.sameorder;

import org.junit.Test;

import ch.hsr.ifs.cute.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.cute.templator.test.FunctionTemplateResolutionTest;

public class OverloadedFunctionWithTemplateArgumentTest extends FunctionTemplateResolutionTest {
    @Test
    public void testSubcallResolvedToDoubleId() throws TemplatorException {
        testFirstInnerCallResolvesToFirstDefinition();
    }

    @Test
    public void testArgumentMapIsDouble() {
        testOuterArgumentMap(DOUBLE);
    }
}
