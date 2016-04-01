package ch.hsr.ifs.templator.test.integrationtest.resolution.function.sameorder;

import org.junit.Test;

import ch.hsr.ifs.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.templator.test.FunctionTemplateResolutionTest;

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
