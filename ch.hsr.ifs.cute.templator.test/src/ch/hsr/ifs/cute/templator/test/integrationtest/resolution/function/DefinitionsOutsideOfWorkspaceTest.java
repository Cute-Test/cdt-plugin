package ch.hsr.ifs.cute.templator.test.integrationtest.resolution.function;

import org.junit.Test;

import ch.hsr.ifs.cute.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.cute.templator.test.FunctionTemplateResolutionTest;

public class DefinitionsOutsideOfWorkspaceTest extends FunctionTemplateResolutionTest {

    @Test
    public void testOuterArgumentMapIsDouble() {
        testOuterArgumentMap(DOUBLE);
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
