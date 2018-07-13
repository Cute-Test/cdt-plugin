package ch.hsr.ifs.cute.mockator.mockobject.asserteq;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.mockator.mockobject.asserteq.AssertKind.ExpectedActualPair;
import ch.hsr.ifs.cute.mockator.mockobject.support.context.MockSupportContext;


abstract class AbstractAssertEqualsInserter {

   protected static final ICPPNodeFactory    nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   protected final ICPPASTFunctionDefinition testFunction;
   protected final MockSupportContext        context;

   public AbstractAssertEqualsInserter(final ICPPASTFunctionDefinition testFunction, final MockSupportContext context) {
      this.testFunction = testFunction;
      this.context = context;
   }

   public void insertAssertEqual(final ASTRewrite rewriter) {
      if (!hasAssertEqual()) {
         insertWith(rewriter);
      }
   }

   private boolean hasAssertEqual() {
      final AssertEqualFinderVisitor assertFinder = new AssertEqualFinderVisitor(Optional.of(context.getMockObject().getKlass()));
      testFunction.accept(assertFinder);
      final Collection<ExpectedActualPair> assertedCalls = assertFinder.getExpectedActual();
      return !assertedCalls.isEmpty();
   }

   protected abstract void insertWith(ASTRewrite rewriter);

   protected void insertAssertEqual(final ASTRewrite rewriter, final IASTNode assertEquals) {
      rewriter.insertBefore(testFunction.getBody(), null, assertEquals, null);
   }

   protected abstract String getNameOfAssert();

   protected IASTArraySubscriptExpression createActual() {
      final IASTName allCalls = nodeFactory.newName(context.getFqNameForAllCallsVector().toCharArray());
      return nodeFactory.newArraySubscriptExpression(nodeFactory.newIdExpression(allCalls), createCallsVectorIndex());
   }

   private IASTExpression createCallsVectorIndex() {
      return nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_integer_constant, context.hasOnlyStaticMemFuns() ? "0" : "1");
   }

   protected IASTIdExpression createExpectations() {
      final IASTName name = nodeFactory.newName(context.getNameForExpectationsVector().toCharArray());
      return nodeFactory.newIdExpression(name);
   }
}
