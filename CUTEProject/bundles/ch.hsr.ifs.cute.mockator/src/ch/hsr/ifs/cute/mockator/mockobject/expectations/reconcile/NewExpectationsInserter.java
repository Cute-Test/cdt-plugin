package ch.hsr.ifs.cute.mockator.mockobject.expectations.reconcile;

import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.head;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.cute.mockator.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.cute.mockator.mockobject.MockObject;
import ch.hsr.ifs.cute.mockator.mockobject.asserteq.AssertEqualFinderVisitor;
import ch.hsr.ifs.cute.mockator.mockobject.asserteq.AssertKind.ExpectedActualPair;
import ch.hsr.ifs.cute.mockator.mockobject.expectations.vector.ExpectationsCppStdStrategy;
import ch.hsr.ifs.cute.mockator.mockobject.expectations.vector.ExpectationsVectorFactory;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.project.properties.LinkedEditModeStrategy;


// Inserts the vector with the expected calls for the mock object at the end of the function:
// calls expected = {{"foo1(int const&) const", int{}}, {"foo2(double const&) const", double{}}};
class NewExpectationsInserter {

    private final ASTRewrite                 rewriter;
    private final ICPPASTFunctionDefinition  testFunction;
    private final ExpectationsCppStdStrategy cppStdStrategy;
    private final LinkedEditModeStrategy     linkedEditStrategy;
    private final MockObject                 mockObject;

    public NewExpectationsInserter(final ICPPASTFunctionDefinition testFunction, final MockObject mockObject, final CppStandard cppStd,
                                   final ASTRewrite rewriter, final LinkedEditModeStrategy linkedEditStrategy) {
        this.testFunction = testFunction;
        this.mockObject = mockObject;
        this.rewriter = rewriter;
        this.linkedEditStrategy = linkedEditStrategy;
        cppStdStrategy = getCppStdStrategy(cppStd);
    }

    public void insertExpectations(final Collection<? extends TestDoubleMemFun> memFunsForExpectations) {
        for (final IASTStatement stmt : createExpectationStatements(memFunsForExpectations)) {
            rewriter.insertBefore(testFunction.getBody(), getInsertionPosition(), stmt, null);
        }
    }

    private Collection<IASTStatement> createExpectationStatements(final Collection<? extends TestDoubleMemFun> memberFunctions) {
        return cppStdStrategy.createExpectationsVector(memberFunctions, createNameForExpectationsVector(), testFunction, getExpectationsVector(),
                linkedEditStrategy);
    }

    private String createNameForExpectationsVector() {
        return mockObject.getNameForExpectationVector();
    }

    private static ExpectationsCppStdStrategy getCppStdStrategy(final CppStandard cppStd) {
        return new ExpectationsVectorFactory(cppStd).getStrategy();
    }

    private IASTStatement getInsertionPosition() {
        final AssertEqualFinderVisitor visitor = new AssertEqualFinderVisitor(Optional.of(mockObject.getKlass()));
        testFunction.accept(visitor);
        final Collection<ExpectedActualPair> assertedCalls = visitor.getExpectedActual();

        if (assertedCalls.isEmpty()) {
            return null; // insert at the end of the test function
        }

        return getStatementOfFirstAssert(assertedCalls);
    }

    private static IASTStatement getStatementOfFirstAssert(final Collection<ExpectedActualPair> assertedCalls) {
        return head(assertedCalls).flatMap(pair -> CPPVisitor.findAncestorWithType(pair.expected(), IASTStatement.class)).orElse(null);
    }

    private Optional<IASTName> getExpectationsVector() {
        final ExpectationsVectorDefinitionFinder finder = new ExpectationsVectorDefinitionFinder(mockObject, testFunction);
        return finder.findExpectationsVector();
    }
}
