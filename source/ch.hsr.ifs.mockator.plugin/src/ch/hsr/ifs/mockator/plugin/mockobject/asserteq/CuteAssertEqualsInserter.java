package ch.hsr.ifs.mockator.plugin.mockobject.asserteq;

import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.mockator.plugin.mockobject.support.context.MockSupportContext;
import ch.hsr.ifs.mockator.plugin.project.properties.AssertionOrder;

@SuppressWarnings("restriction")
class CuteAssertEqualsInserter extends AbstractAssertEqualsInserter {

  public CuteAssertEqualsInserter(ICPPASTFunctionDefinition testFunction, MockSupportContext context) {
    super(testFunction, context);
  }

  @Override
  protected void insertWith(ASTRewrite rewriter) {
    IASTExpressionStatement assertEqual = createAssertEqualStmt();
    insertAssertEqual(rewriter, assertEqual);
  }

  private IASTExpressionStatement createAssertEqualStmt() {
    IASTInitializerClause[] initializer =
        new IASTInitializerClause[] {createExpectations(), createActual()};
    IASTFunctionCallExpression assertEqual =
        nodeFactory.newFunctionCallExpression(getAssertMacroId(), initializer);
    return nodeFactory.newExpressionStatement(assertEqual);
  }

  private IASTIdExpression getAssertMacroId() {
    return nodeFactory.newIdExpression(nodeFactory.newName(getNameOfAssert().toCharArray()));
  }

  @Override
  protected String getNameOfAssert() {
    return AssertionOrder.fromProjectSettings(context.getProject().getProject())
        .getAssertionCommand().toString();
  }
}
