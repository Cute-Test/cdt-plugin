package ch.hsr.ifs.templator.test.asttests;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.junit.Test;

import ch.hsr.ifs.templator.plugin.asttools.ASTAnalyzer;
import ch.hsr.ifs.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.templator.test.TemplatorProjectTest;
import ch.hsr.ifs.templator.test.TestHelper;

public class ResolveTargetBindingTest extends TemplatorProjectTest {

    @Test
    // void bar() {}
    // int main() {
    // bar();
    // }
    public void testResolveTargetBindingNonIndexBinding() throws Exception {
        String source = TestHelper.getCommentAbove(getClass());

        IASTTranslationUnit ast = TestHelper.parse(source, ParserLanguage.CPP);
        ASTAnalyzer analyzer = new ASTAnalyzer(null, ast);

        IASTName occurance = TestHelper.findName(ast, 1, "bar");

        IBinding actual = analyzer.resolveTargetBinding(occurance);
        IBinding expected = occurance.resolveBinding();

        assertEquals(expected, actual);
    }

    @Test
    public void testResolveTargetBindingToIndexBindingAdaptBindingEnabled() throws TemplatorException {
        IASTFunctionDefinition main = getMain();
        IASTName functionCallName = getSubStatements(main).get(0).getResolvingName();

        IBinding resolveTargetBinding = analyzer.resolveTargetBinding(functionCallName);
        assertTrue(resolveTargetBinding instanceof IIndexBinding);
    }
}
