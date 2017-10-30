package ch.hsr.ifs.mockator.plugin.mockobject.expectations.qf;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._1;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._2;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.tuples.Pair;
import ch.hsr.ifs.mockator.plugin.base.tuples.Tuple;
import ch.hsr.ifs.mockator.plugin.mockobject.asserteq.AssertEqualFinderVisitor;
import ch.hsr.ifs.mockator.plugin.mockobject.asserteq.AssertKind.ExpectedActualPair;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.MemFunCallExpectation;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.finder.ExpectationsFinder;
import ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder.ExistingMemFunCallRegistration;
import ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls.CallsVectorTypeVerifier;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.testdouble.support.MemFunSignature;
import ch.hsr.ifs.mockator.plugin.testdouble.support.TestFunctionChecker;


public class InconsistentExpectationsChecker extends TestFunctionChecker {

   public static final String INCONSISTENT_EXPECTATIONS_PROBLEM_ID = "ch.hsr.ifs.mockator.InconsistentExpectationsProblem";

   @Override
   protected void processTestFunction(final IASTFunctionDefinition testFun) {
      for (final ExpectedActualPair expectedActual : getAssertedCalls(testFun)) {
         getExpectationsAndRegistrations(expectedActual).ifPresent((expReg) -> {
            final Pair<Collection<MemFunCallExpectation>, IASTName> expectations = getExpectations(testFun, _1(expReg));
            markDiffsIfNecessary(_1(expectations), _2(expectations), getCallRegistrations(_2(expReg)));
         });
      }
   }

   private static Optional<Pair<IASTIdExpression, IASTIdExpression>> getExpectationsAndRegistrations(final ExpectedActualPair expectedActual) {
      final IASTIdExpression fstInAssert = _1(expectedActual);
      final IASTIdExpression sndInAssert = _2(expectedActual);

      if (isOfExpectationsType(fstInAssert) && isOfRegistrationsType(sndInAssert)) {
         return Optional.of(Tuple.from(fstInAssert, sndInAssert));
      } else if (isOfRegistrationsType(fstInAssert) && isOfExpectationsType(sndInAssert)) {
         return Optional.of(Tuple.from(sndInAssert, fstInAssert));
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
      final Collection<MemFunSignature> diff = orderPreservingSet();

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
      }
      catch (final CoreException e) {
         throw new MockatorException(e);
      }
   }

   private CppStandard getCppStandard() {
      return CppStandard.fromCompilerFlags(getProject());
   }

   private void mark(final IASTName toMark, final Collection<MemFunSignature> toRemove, final Collection<MemFunSignature> toAdd) {
      reportProblem(INCONSISTENT_EXPECTATIONS_PROBLEM_ID, toMark, getCodanArgs(toRemove, toAdd));
   }

   private static Object[] getCodanArgs(final Collection<MemFunSignature> toRemove, final Collection<MemFunSignature> toAdd) {
      return new ConsistentExpectationsCodanArgs(toRemove, toAdd).toArray();
   }

   private static Pair<Collection<MemFunCallExpectation>, IASTName> getExpectations(final IASTFunctionDefinition function,
            final IASTIdExpression expectedCalls) {
      final ExpectationsFinder finder = new ExpectationsFinder(function);
      return finder.getExpectations(expectedCalls.getName());
   }

   private static Collection<ExpectedActualPair> getAssertedCalls(final IASTFunctionDefinition function) {
      final AssertEqualFinderVisitor visitor = new AssertEqualFinderVisitor(Optional.empty());
      function.accept(visitor);
      return visitor.getExpectedActual();
   }
}
