package ch.hsr.ifs.cute.mockator.mockobject.expectations.finder;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Sets;

import ch.hsr.ifs.iltis.core.data.AbstractPair;
import ch.hsr.ifs.iltis.core.exception.ILTISException;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.cute.mockator.mockobject.expectations.MemFunCallExpectation;
import ch.hsr.ifs.cute.mockator.refsupport.utils.NodeContainer;


public class ExpectationsFinder {

    private final MutableSet<MemFunCallExpectation> callExpectations;
    private final NodeContainer<IASTName>           expectationVector;
    private final IASTFunctionDefinition            testFunction;

    public ExpectationsFinder(final IASTFunctionDefinition testFunction) {
        this.testFunction = testFunction;
        callExpectations = Sets.mutable.empty();
        expectationVector = new NodeContainer<>();
    }

    public ExpectionsInfo getExpectations(final IASTName assertedExpectation) {
        testFunction.accept(new ASTVisitor() {

            {
                shouldVisitNames = true;
            }

            @Override
            public int visit(final IASTName name) {
                if (!nameMatches(name)) {
                    return PROCESS_SKIP;
                }

                return collectExpectations(name);
            }

            private boolean nameMatches(final IASTName name) {
                return name.toString().equals(assertedExpectation.toString());
            }

            private int collectExpectations(final IASTName name) {
                final IASTStatement stmt = CPPVisitor.findAncestorWithType(name, IASTStatement.class).orElse(null);

                if (stmt instanceof IASTDeclarationStatement) {
                    new InitializerExpectationsFinder(callExpectations, expectationVector, assertedExpectation).collectExpectations(stmt);
                } else if (stmt instanceof IASTExpressionStatement) {
                    new BoostVectorExpectationsFinder(callExpectations, expectationVector, assertedExpectation).collectExpectations(stmt);
                }

                if (callExpectations.isEmpty()) {
                    return PROCESS_CONTINUE;
                }

                return PROCESS_ABORT;
            }
        });

        return new ExpectionsInfo(callExpectations, getNameOfExpectationVector(assertedExpectation));
    }

    private IASTName getNameOfExpectationVector(final IASTName assertedExpectation) {
        return expectationVector.getNode().orElseGet(() -> {
            final IBinding binding = assertedExpectation.resolveBinding();
            final IASTName[] definitions = assertedExpectation.getTranslationUnit().getDefinitionsInAST(binding);
            ILTISException.Unless.isTrue("Expectation vector must have a definition", definitions.length > 0);
            return definitions[0];
        });
    }

    public class ExpectionsInfo extends AbstractPair<MutableSet<MemFunCallExpectation>, IASTName> {

        public ExpectionsInfo(final MutableSet<MemFunCallExpectation> expectations, final IASTName vectorName) {
            super(expectations, vectorName);
        }

        public MutableSet<MemFunCallExpectation> getExpectations() {
            return first;
        }

        public IASTName getAssignExpectationsVector() {
            return second;
        }

    }
}
