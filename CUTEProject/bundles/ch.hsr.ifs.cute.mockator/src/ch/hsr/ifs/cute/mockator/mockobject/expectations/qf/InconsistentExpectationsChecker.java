package ch.hsr.ifs.cute.mockator.mockobject.expectations.qf;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.iltis.core.data.AbstractPair;
import ch.hsr.ifs.iltis.core.exception.ILTISException;

import ch.hsr.ifs.iltis.cpp.core.ast.checker.VisitorReport;
import ch.hsr.ifs.iltis.cpp.core.collections.StringList;

import ch.hsr.ifs.cute.mockator.ids.IdHelper.ProblemId;
import ch.hsr.ifs.cute.mockator.infos.ConsistentExpectationsInfo;
import ch.hsr.ifs.cute.mockator.mockobject.asserteq.AssertEqualFinderVisitor;
import ch.hsr.ifs.cute.mockator.mockobject.asserteq.AssertKind.ExpectedActualPair;
import ch.hsr.ifs.cute.mockator.mockobject.expectations.MemFunCallExpectation;
import ch.hsr.ifs.cute.mockator.mockobject.expectations.finder.ExpectationsFinder;
import ch.hsr.ifs.cute.mockator.mockobject.expectations.finder.ExpectationsFinder.ExpectionsInfo;
import ch.hsr.ifs.cute.mockator.mockobject.registrations.finder.ExistingMemFunCallRegistration;
import ch.hsr.ifs.cute.mockator.mockobject.support.allcalls.CallsVectorTypeVerifier;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.testdouble.support.MemFunSignature;
import ch.hsr.ifs.cute.mockator.testdouble.support.TestFunctionChecker;


public class InconsistentExpectationsChecker extends TestFunctionChecker {

    @Override
    protected void processTestFunction(final VisitorReport<ProblemId> result) {
        final IASTFunctionDefinition testFun = (IASTFunctionDefinition) result.getNode();
        for (final ExpectedActualPair expectedActual : getAssertedCalls(testFun)) {
            getExpectationsAndRegistrations(expectedActual).ifPresent((expReg) -> {
                final ExpectionsInfo expectations = getExpectations(testFun, expReg.getExpectations());
                markDiffsIfNecessary(new FastList<>(expectations.getExpectations()), expectations.getAssignExpectationsVector(), getCallRegistrations(
                        expReg.getRegistrations()));
            });
        }
    }

    private static Optional<ExpectedAndRegistration> getExpectationsAndRegistrations(final ExpectedActualPair expectedActual) {
        final IASTIdExpression fstInAssert = expectedActual.expected();
        final IASTIdExpression sndInAssert = expectedActual.actual();

        if (isOfExpectationsType(fstInAssert) && isOfRegistrationsType(sndInAssert)) {
            return Optional.of(new ExpectedAndRegistration(fstInAssert, sndInAssert));
        } else if (isOfRegistrationsType(fstInAssert) && isOfExpectationsType(sndInAssert)) {
            return Optional.of(new ExpectedAndRegistration(sndInAssert, fstInAssert));
        } else {
            return Optional.empty();
        }
    }

    private static boolean isOfExpectationsType(final IASTIdExpression idExpr) {
        return new CallsVectorTypeVerifier(idExpr).hasCallsVectorType();
    }

    private static boolean isOfRegistrationsType(final IASTIdExpression idExpr) {
        return new CallsVectorTypeVerifier(idExpr).isVectorOfCallsVector();
    }

    private void markDiffsIfNecessary(final MutableList<MemFunCallExpectation> expectedCalls, final IASTName toMark,
            final MutableList<ExistingMemFunCallRegistration> callRegistrations) {

        final MutableList<MemFunCallExpectation> toRemove = orderPreservingDiff(expectedCalls, callRegistrations);
        final MutableList<ExistingMemFunCallRegistration> toAdd = orderPreservingDiff(callRegistrations, expectedCalls);

        if (!toRemove.isEmpty() || !toAdd.isEmpty()) {
            mark(toMark, toRemove, toAdd);
        }
    }

    private static <T extends MemFunSignature> MutableList<T> orderPreservingDiff(final MutableList<T> setA,
            final MutableList<? extends MemFunSignature> setB) {
        return setA.select(m -> m.isCovered(setB), Lists.mutable.<T>ofInitialCapacity(setA.size()));
    }

    private MutableList<ExistingMemFunCallRegistration> getCallRegistrations(final IASTIdExpression vector) {
        try {
            final IASTTranslationUnit ast = getModelCache().getAST();
            final RegistrationCandidatesFinder finder = new RegistrationCandidatesFinder(ast, getCppStandard());
            return finder.findCallRegistrations(vector.getName());
        } catch (final CoreException e) {
            throw new ILTISException(e).rethrowUnchecked();
        }
    }

    private CppStandard getCppStandard() {
        return CppStandard.fromCompilerFlags(getProject());
    }

    private void mark(final IASTName toMark, final MutableList<MemFunCallExpectation> toRemove,
            final MutableList<ExistingMemFunCallRegistration> toAdd) {
        addNodeForReporting(new VisitorReport<>(getProblemId(), toMark), getInfo(toRemove, toAdd));
    }

    private static ConsistentExpectationsInfo getInfo(final MutableList<MemFunCallExpectation> toRemove,
            final MutableList<ExistingMemFunCallRegistration> toAdd) {
        return new ConsistentExpectationsInfo().also(i -> {
            i.expectationsToAdd = toAdd.collect(MemFunSignature::toString, StringList.newList(toAdd.size()));
            i.expectationsToRemove = toRemove.collect(MemFunSignature::toString, StringList.newList(toRemove.size()));
        });
    }

    private static ExpectionsInfo getExpectations(final IASTFunctionDefinition function, final IASTIdExpression expectedCalls) {
        final ExpectationsFinder finder = new ExpectationsFinder(function);
        return finder.getExpectations(expectedCalls.getName());
    }

    private static Collection<ExpectedActualPair> getAssertedCalls(final IASTFunctionDefinition function) {
        final AssertEqualFinderVisitor visitor = new AssertEqualFinderVisitor(Optional.empty());
        function.accept(visitor);
        return visitor.getExpectedActual();
    }

    @Override
    protected ProblemId getProblemId() {
        return ProblemId.INCONSISTENT_EXPECTATIONS;
    }

    private static class ExpectedAndRegistration extends AbstractPair<IASTIdExpression, IASTIdExpression> {

        public ExpectedAndRegistration(final IASTIdExpression expected, final IASTIdExpression registration) {
            super(expected, registration);
        }

        public IASTIdExpression getExpectations() {
            return first;
        }

        public IASTIdExpression getRegistrations() {
            return second;
        }

    }

}
