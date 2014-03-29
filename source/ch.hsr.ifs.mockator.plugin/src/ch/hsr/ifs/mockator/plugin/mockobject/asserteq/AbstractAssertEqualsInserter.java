package ch.hsr.ifs.mockator.plugin.mockobject.asserteq;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.mockobject.asserteq.AssertKind.ExpectedActualPair;
import ch.hsr.ifs.mockator.plugin.mockobject.support.context.MockSupportContext;

@SuppressWarnings("restriction")
abstract class AbstractAssertEqualsInserter {
  protected static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();
  protected final ICPPASTFunctionDefinition testFunction;
  protected final MockSupportContext context;

  public AbstractAssertEqualsInserter(ICPPASTFunctionDefinition testFunction,
      MockSupportContext context) {
    this.testFunction = testFunction;
    this.context = context;
  }

  public void insertAssertEqual(ASTRewrite rewriter) {
    if (!hasAssertEqual()) {
      insertWith(rewriter);
    }
  }

  private boolean hasAssertEqual() {
    AssertEqualFinderVisitor assertFinder =
        new AssertEqualFinderVisitor(maybe(context.getMockObject().getKlass()));
    testFunction.accept(assertFinder);
    Collection<ExpectedActualPair> assertedCalls = assertFinder.getExpectedActual();
    return !assertedCalls.isEmpty();
  }

  protected abstract void insertWith(ASTRewrite rewriter);

  protected void insertAssertEqual(ASTRewrite rewriter, IASTNode assertEquals) {
    rewriter.insertBefore(testFunction.getBody(), null, assertEquals, null);
  }

  protected abstract String getNameOfAssert();

  protected IASTArraySubscriptExpression createActual() {
    IASTName allCalls = nodeFactory.newName(context.getFqNameForAllCallsVector().toCharArray());
    return nodeFactory.newArraySubscriptExpression(nodeFactory.newIdExpression(allCalls),
        createCallsVectorIndex());
  }

  private IASTExpression createCallsVectorIndex() {
    return nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_integer_constant,
        context.hasOnlyStaticMemFuns() ? "0" : "1");
  }

  protected IASTIdExpression createExpectations() {
    IASTName name = nodeFactory.newName(context.getNameForExpectationsVector().toCharArray());
    return nodeFactory.newIdExpression(name);
  }
}
