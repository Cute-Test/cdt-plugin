package ch.hsr.ifs.templator.test.asttests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.junit.Test;

import ch.hsr.ifs.templator.plugin.asttools.ASTAnalyzer;
import ch.hsr.ifs.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.templator.test.TemplatorSimpleTest;
import ch.hsr.ifs.templator.test.TestHelper;

public class GetFunctionBindingTest extends TemplatorSimpleTest {

    @Test
    // template<typename T>
    // T bar(T value) {
    // return value * 42;
    // }
    // int main() {
    // bar(-30.0);
    // }
    public void testGetFunctionTemplateBindingFromTemplateWithoutAngleBrackets() throws Exception {
        String source = TestHelper.getCommentAbove(getClass());
        IASTTranslationUnit ast = TestHelper.parse(source, ParserLanguage.CPP);
        ASTAnalyzer analyzer = new ASTAnalyzer(null, ast);

        IASTName occurance = TestHelper.findName(ast, 1, "bar");

        IBinding binding = occurance.resolveBinding();

        IFunction templateBinding = analyzer.getFunctionBinding(binding);

        assertNotNull(templateBinding);
        assertTrue(templateBinding instanceof ICPPFunctionTemplate);
    }

    @Test
    // template<typename T>
    // T bar(T value) {
    // return value * 42;
    // }
    // int main() {
    // bar<int>(-30.0);
    // }
    public void testGetFunctionTemplateBindingFromTemplateWithAngleBrackets() throws Exception {

        String source = TestHelper.getCommentAbove(getClass());
        IASTTranslationUnit ast = TestHelper.parse(source, ParserLanguage.CPP);
        ASTAnalyzer analyzer = new ASTAnalyzer(null, ast);

        IASTName occurance = TestHelper.findName(ast, 1, "bar");

        IBinding binding = occurance.resolveBinding();

        IFunction templateBinding = analyzer.getFunctionBinding(binding);

        assertNotNull(templateBinding);
        assertTrue(templateBinding instanceof ICPPFunctionTemplate);
    }

    @Test
    //
    // bool numberwang(int i) {
    // return true;
    // }
    // int main() {
    // numberwang(13);
    // }
    public void testGetFunctionBindingFromNormalFunction() throws Exception {

        String source = TestHelper.getCommentAbove(getClass());
        IASTTranslationUnit ast = TestHelper.parse(source, ParserLanguage.CPP);
        ASTAnalyzer analyzer = new ASTAnalyzer(null, ast);

        IASTName occurance = TestHelper.findName(ast, 1, "numberwang");

        IBinding binding = occurance.resolveBinding();

        IFunction templateBinding = analyzer.getFunctionBinding(binding);

        assertNotNull(templateBinding);
        assertTrue(templateBinding instanceof ICPPFunction);
    }

    @Test(expected = TemplatorException.class)
    // int main() {
    // int bar = 10;
    // }
    public void testGetFunctionTemplateBindingNotAFunctionBinding() throws Exception {
        String source = TestHelper.getCommentAbove(getClass());
        IASTTranslationUnit ast = TestHelper.parse(source, ParserLanguage.CPP);
        ASTAnalyzer analyzer = new ASTAnalyzer(null, ast);

        IASTName occurance = TestHelper.findName(ast, 0, "bar");

        IBinding binding = occurance.resolveBinding();
        analyzer.getFunctionBinding(binding);
    }
}
