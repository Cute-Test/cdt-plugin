package ch.hsr.ifs.templator.test.integrationtest.resolution.function;

import org.junit.Test;

import ch.hsr.ifs.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.templator.test.FunctionTemplateResolutionTest;

public class ClassKeywordInsteadOfTypename extends FunctionTemplateResolutionTest {

    @Test
    public void testOuterArgumentMapIsInt() {
        testOuterArgumentMap(INT);
    }

    @Test
    public void testSubcallArgumentIsInt() throws TemplatorException {
        testFirstInnerArgumentMap(INT);
    }

    @Test
    public void testSubcallResolvedToFunctionTemplateAndNotNormalFunction() throws TemplatorException {
        testFirstInnerCallResolvesToFirstDefinition();
    }
}
