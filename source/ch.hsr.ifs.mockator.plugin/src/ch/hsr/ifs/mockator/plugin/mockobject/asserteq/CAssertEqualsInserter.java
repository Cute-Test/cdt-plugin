package ch.hsr.ifs.mockator.plugin.mockobject.asserteq;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.mockobject.support.context.MockSupportContext;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.AstIncludeNode;


class CAssertEqualsInserter extends AbstractAssertEqualsInserter {

   public CAssertEqualsInserter(final ICPPASTFunctionDefinition testFunction, final MockSupportContext context) {
      super(testFunction, context);
   }

   @Override
   protected void insertWith(final ASTRewrite rewriter) {
      insertCAssertInclude(rewriter);
      final IASTExpressionStatement cAssert = createAssertEqualStmt();
      insertAssertEqual(rewriter, cAssert);
   }

   private void insertCAssertInclude(final ASTRewrite rewriter) {
      final AstIncludeNode cAssert = new AstIncludeNode(MockatorConstants.C_ASSERT_INCLUDE, true);
      cAssert.insertInTu(testFunction.getTranslationUnit(), rewriter);
   }

   @SuppressWarnings("restriction")
   private IASTExpressionStatement createAssertEqualStmt() {
      final IASTFunctionCallExpression assertEqual = nodeFactory.newFunctionCallExpression(createCAssert(), getAssertEqualParams());
      return nodeFactory.newExpressionStatement(assertEqual);
   }

   @SuppressWarnings("restriction")
   private IASTInitializerClause[] getAssertEqualParams() {
      final ICPPASTBinaryExpression binOp = nodeFactory.newBinaryExpression(IASTBinaryExpression.op_equals, createExpectations(), createActual());
      return new IASTInitializerClause[] { binOp };
   }

   @SuppressWarnings("restriction")
   private IASTIdExpression createCAssert() {
      return nodeFactory.newIdExpression(nodeFactory.newName(getNameOfAssert().toCharArray()));
   }

   @Override
   protected String getNameOfAssert() {
      return MockatorConstants.C_ASSERT_EQUAL;
   }
}
