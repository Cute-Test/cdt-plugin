package ch.hsr.ifs.templator.test.asttests;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingTest;
import ch.hsr.ifs.templator.plugin.asttools.data.NameTypeKind;
import ch.hsr.ifs.templator.test.TestHelper;

public class IsFunctionTemplateInstanceTest extends CDTTestingTest {

    @Test
    public void testIsTemplateFunctionInstanceFromNameGoodCase() throws Exception {

        String source = fileMap.get(activeFileName).getSource();
        IASTTranslationUnit ast = TestHelper.parse(source, ParserLanguage.CPP);

        IASTNode node = TestHelper.findName(ast, 0, "foo");
        assertTrue(node instanceof IASTName);
        assertFalse(NameTypeKind.isFunctionTemplateInstance(((IASTName) node).resolveBinding()));

        node = TestHelper.findName(ast, 1, "foo");
        assertTrue(node instanceof IASTName);
        assertTrue(NameTypeKind.isFunctionTemplateInstance(((IASTName) node).resolveBinding()));
    }

    @Test
    public void testIsTemplateFunctionInstanceFromNameBadCase() throws Exception {

        String source = fileMap.get(activeFileName).getSource();
        IASTTranslationUnit ast = TestHelper.parse(source, ParserLanguage.CPP);

        IASTNode node = TestHelper.findName(ast, 0, "bar");
        assertTrue(node instanceof IASTName);
        assertFalse(NameTypeKind.isFunctionTemplateInstance(((IASTName) node).resolveBinding()));

        node = TestHelper.findName(ast, 1, "bar");
        assertTrue(node instanceof IASTName);
        assertFalse(NameTypeKind.isFunctionTemplateInstance(((IASTName) node).resolveBinding()));
    }
}
