package ch.hsr.ifs.cute.mockator.mockobject.expectations.reconcile;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.cute.mockator.mockobject.MockObject;
import ch.hsr.ifs.cute.mockator.mockobject.asserteq.AssertEqualFinderVisitor;
import ch.hsr.ifs.cute.mockator.mockobject.asserteq.AssertKind.ExpectedActualPair;
import ch.hsr.ifs.cute.mockator.refsupport.finder.NameFinder;


class ExpectationsVectorDefinitionFinder {

    private final ICPPASTFunctionDefinition testFunction;
    private final MockObject                mockObject;

    public ExpectationsVectorDefinitionFinder(final MockObject mockObject, final ICPPASTFunctionDefinition testFun) {
        this.mockObject = mockObject;
        testFunction = testFun;
    }

    public Optional<IASTName> findExpectationsVector() {
        return mockObject.getRegistrationVector().flatMap(regVector -> {
            for (final ExpectedActualPair expectedActual : findAssertedCallsInTestFunction()) {
                final Optional<IASTName> expVector = getExpectationsVector(expectedActual, regVector);
                if (expVector.isPresent()) {
                    return getNameOfDefinition(expVector.get());
                }
            }
            return Optional.empty();
        });
    }

    private Optional<IASTName> getNameOfDefinition(final IASTName expectationsVector) {
        return new NameFinder(testFunction).getNameMatchingCriteria((name) -> name.toString().equals(expectationsVector.toString()) && CPPVisitor
                .findAncestorWithType(name, IASTDeclarationStatement.class).orElse(null) != null);
    }

    private static Optional<IASTName> getExpectationsVector(final ExpectedActualPair expectedActual, final IASTName registrationVector) {
        final IBinding vectorBinding = registrationVector.resolveBinding();
        final IASTName candidate1 = expectedActual.expected().getName();
        final IASTName candidate2 = expectedActual.actual().getName();

        if (vectorBinding.equals(candidate1.resolveBinding())) {
            return Optional.of(candidate2);
        } else if (vectorBinding.equals(candidate2.resolveBinding())) {
            return Optional.of(candidate1);
        } else {
            return Optional.empty();
        }
    }

    private Collection<ExpectedActualPair> findAssertedCallsInTestFunction() {
        final AssertEqualFinderVisitor visitor = new AssertEqualFinderVisitor(Optional.of(mockObject.getKlass()));
        testFunction.accept(visitor);
        return visitor.getExpectedActual();
    }
}
