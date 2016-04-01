package ch.hsr.ifs.templator.test.integrationtest.resolution.function;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.junit.Test;

import ch.hsr.ifs.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.templator.test.FunctionTemplateResolutionTest;

public class FunctionThatIsInNamespaceResolutionTest extends FunctionTemplateResolutionTest {

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
        ICPPASTNamespaceDefinition outerNamespace = (ICPPASTNamespaceDefinition) analyzer.getAst().getDeclarations()[0];
        ICPPASTNamespaceDefinition innerNamespace = (ICPPASTNamespaceDefinition) outerNamespace.getDeclarations()[0];
        IASTDeclaration expected = ((ICPPASTTemplateDeclaration) innerNamespace.getDeclarations()[0]).getDeclaration();
        testFirstInnerCallResolvesTo((IASTFunctionDefinition) expected);
    }
}
