package ch.hsr.ifs.templator.test;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;

public class ClassTemplateResolutionTest extends TemplatorProjectTest {

    protected void firstStatementResolvesToFirstDefinition() {
        firstStatementResolvesToDefinition(definitions.get(0));
    }

    protected void firstStatementResolvesToDefinition(IASTDeclaration definition) {
        assertEquals(definition, firstStatementInMain.getDefinition());
    }
}
