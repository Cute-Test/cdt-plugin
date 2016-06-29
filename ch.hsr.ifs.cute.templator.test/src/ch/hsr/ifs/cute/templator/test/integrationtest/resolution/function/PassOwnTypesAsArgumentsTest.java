package ch.hsr.ifs.cute.templator.test.integrationtest.resolution.function;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateTypeArgument;
import org.junit.Test;

import ch.hsr.ifs.cute.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.cute.templator.test.FunctionTemplateResolutionTest;

public class PassOwnTypesAsArgumentsTest extends FunctionTemplateResolutionTest {

    @Test
    public void testOuterArgumentMapIsInt() {
        testOuterArgumentMap(INT);
    }

    @Test
    public void testSubcallArgumentMapIsMyClassMyClassPointer() throws TemplatorException {
        IType myClassType = new CPPClassType(factory.newName("MyClass".toCharArray()), null);
        IType myClassPointerType = new CPPPointerType(myClassType);
        testFirstInnerArgumentMap(new CPPTemplateTypeArgument(myClassType), new CPPTemplateTypeArgument(
                myClassPointerType));
    }

    @Test
    public void testSubcallResolvedToFunctionTemplateAndNotNormalFunction() throws TemplatorException {
        testFirstInnerCallResolvesToFirstDefinition();
    }
}
