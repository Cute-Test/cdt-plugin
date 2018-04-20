package ch.hsr.ifs.mockator.plugin.mockobject.expectations.qf;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.iltis.core.core.data.AbstractPair;
import ch.hsr.ifs.iltis.core.core.exception.ILTISException;
import ch.hsr.ifs.iltis.cpp.core.ast.checker.CheckerResult;

import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;
import ch.hsr.ifs.mockator.plugin.mockobject.asserteq.AssertEqualFinderVisitor;
import ch.hsr.ifs.mockator.plugin.mockobject.asserteq.AssertKind.ExpectedActualPair;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.MemFunCallExpectation;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.finder.ExpectationsFinder;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.finder.ExpectationsFinder.ExpectionsInfo;
import ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder.ExistingMemFunCallRegistration;
import ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls.CallsVectorTypeVerifier;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.testdouble.support.MemFunSignature;
import ch.hsr.ifs.mockator.plugin.testdouble.support.TestFunctionChecker;


public class InconsistentExpectationsChecker extends TestFunctionChecker {

   @Override
   protected void processTestFunction(final CheckerResult<ProblemId> result) {
      final IASTFunctionDefinition testFun = (IASTFunctionDefinition) result.getNode();
      for (final ExpectedActualPair expectedActual : getAssertedCalls(testFun)) {
         getExpectationsAndRegistrations(expectedActual).ifPresent((expReg) -> {
            final ExpectionsInfo expectations = getExpectations(testFun, expReg.getExpectations());
            markDiffsIfNecessary(expectations.getExpectations(), expectations.getAssignExpectationsVector(), getCallRegistrations(expReg
                  .getRegistrations()));
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

   private void markDiffsIfNecessary(final Collection<MemFunCallExpectation> expectedCalls, final IASTName toMark,
         final Collection<ExistingMemFunCallRegistration> callRegistrations) {
      final Collection<MemFunSignature> toRemove = orderPreservingDiff(expectedCalls, callRegistrations);
      final Collection<MemFunSignature> toAdd = orderPreservingDiff(callRegistrations, expectedCalls);

      if (!toRemove.isEmpty() || !toAdd.isEmpty()) {
         mark(toMark, toRemove, toAdd);
      }
   }

   private static Collection<MemFunSignature> orderPreservingDiff(final Collection<? extends MemFunSignature> setA,
         final Collection<? extends MemFunSignature> setB) {
      final Collection<MemFunSignature> diff = new LinkedHashSet<>();

      for (final MemFunSignature signature : setA) {
         if (!signature.isCovered(setB)) {
            diff.add(signature);
         }
      }
      return diff;
   }

   private Collection<ExistingMemFunCallRegistration> getCallRegistrations(final IASTIdExpression vector) {
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

   private void mark(final IASTName toMark, final Collection<MemFunSignature> toRemove, final Collection<MemFunSignature> toAdd) {
      addNodeForReporting(new CheckerResult<>(getProblemId(), toMark), getCodanArgs(toRemove, toAdd));
   }

   private static Object[] getCodanArgs(final Collection<MemFunSignature> toRemove, final Collection<MemFunSignature> toAdd) {
      return new ConsistentExpectationsCodanArgs(toRemove, toAdd).toArray();
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

      public ExpectedAndRegistration(IASTIdExpression expected, IASTIdExpression registration) {
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
