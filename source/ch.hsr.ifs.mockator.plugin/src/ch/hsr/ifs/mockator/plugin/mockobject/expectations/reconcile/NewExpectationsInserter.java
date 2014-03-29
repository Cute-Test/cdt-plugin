package ch.hsr.ifs.mockator.plugin.mockobject.expectations.reconcile;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.head;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._1;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.mockobject.MockObject;
import ch.hsr.ifs.mockator.plugin.mockobject.asserteq.AssertEqualFinderVisitor;
import ch.hsr.ifs.mockator.plugin.mockobject.asserteq.AssertKind.ExpectedActualPair;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.vector.ExpectationsCppStdStrategy;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.vector.ExpectationsVectorFactory;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

// Inserts the vector with the expected calls for the mock object at the end of the function:
// calls expected = {{"foo1(int const&) const", int{}}, {"foo2(double const&) const", double{}}};
class NewExpectationsInserter {
  private final ASTRewrite rewriter;
  private final ICPPASTFunctionDefinition testFunction;
  private final ExpectationsCppStdStrategy cppStdStrategy;
  private final LinkedEditModeStrategy linkedEditStrategy;
  private final MockObject mockObject;

  public NewExpectationsInserter(ICPPASTFunctionDefinition testFunction, MockObject mockObject,
      CppStandard cppStd, ASTRewrite rewriter, LinkedEditModeStrategy linkedEditStrategy) {
    this.testFunction = testFunction;
    this.mockObject = mockObject;
    this.rewriter = rewriter;
    this.linkedEditStrategy = linkedEditStrategy;
    cppStdStrategy = getCppStdStrategy(cppStd);
  }

  public void insertExpectations(Collection<? extends TestDoubleMemFun> memFunsForExpectations) {
    for (IASTStatement stmt : createExpectationStatements(memFunsForExpectations)) {
      rewriter.insertBefore(testFunction.getBody(), getInsertionPosition(), stmt, null);
    }
  }

  private Collection<IASTStatement> createExpectationStatements(
      Collection<? extends TestDoubleMemFun> memberFunctions) {
    return cppStdStrategy.createExpectationsVector(memberFunctions,
        createNameForExpectationsVector(), testFunction, getExpectationsVector(),
        linkedEditStrategy);
  }

  private String createNameForExpectationsVector() {
    return mockObject.getNameForExpectationVector();
  }

  private static ExpectationsCppStdStrategy getCppStdStrategy(CppStandard cppStd) {
    return new ExpectationsVectorFactory(cppStd).getStrategy();
  }

  private IASTStatement getInsertionPosition() {
    AssertEqualFinderVisitor visitor = new AssertEqualFinderVisitor(maybe(mockObject.getKlass()));
    testFunction.accept(visitor);
    Collection<ExpectedActualPair> assertedCalls = visitor.getExpectedActual();

    if (assertedCalls.isEmpty())
      return null; // insert at the end of the test function

    return getStatementOfFirstAssert(assertedCalls);
  }

  private static IASTStatement getStatementOfFirstAssert(
      Collection<ExpectedActualPair> assertedCalls) {
    for (ExpectedActualPair optPair : head(assertedCalls))
      return AstUtil.getAncestorOfType(_1(optPair), IASTStatement.class);

    return null;
  }

  private Maybe<IASTName> getExpectationsVector() {
    ExpectationsVectorDefinitionFinder finder =
        new ExpectationsVectorDefinitionFinder(mockObject, testFunction);
    return finder.findExpectationsVector();
  }
}
