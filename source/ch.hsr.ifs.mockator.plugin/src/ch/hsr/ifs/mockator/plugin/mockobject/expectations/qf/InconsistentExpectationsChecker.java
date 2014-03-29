package ch.hsr.ifs.mockator.plugin.mockobject.expectations.qf;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._1;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._2;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
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
  public static final String INCONSISTENT_EXPECTATIONS_PROBLEM_ID =
      "ch.hsr.ifs.mockator.InconsistentExpectationsProblem";

  @Override
  protected void processTestFunction(IASTFunctionDefinition testFun) {
    for (ExpectedActualPair expectedActual : getAssertedCalls(testFun)) {
      for (Pair<IASTIdExpression, IASTIdExpression> optExpReg : getExpectationsAndRegistrations(expectedActual)) {
        Pair<Collection<MemFunCallExpectation>, IASTName> expectations =
            getExpectations(testFun, _1(optExpReg));
        markDiffsIfNecessary(_1(expectations), _2(expectations),
            getCallRegistrations(_2(optExpReg)));
      }
    }
  }

  private static Maybe<Pair<IASTIdExpression, IASTIdExpression>> getExpectationsAndRegistrations(
      ExpectedActualPair expectedActual) {
    IASTIdExpression fstInAssert = _1(expectedActual);
    IASTIdExpression sndInAssert = _2(expectedActual);

    if (isOfExpectationsType(fstInAssert) && isOfRegistrationsType(sndInAssert))
      return maybe(Tuple.from(fstInAssert, sndInAssert));
    else if (isOfRegistrationsType(fstInAssert) && isOfExpectationsType(sndInAssert))
      return maybe(Tuple.from(sndInAssert, fstInAssert));
    else
      return none();
  }

  private static boolean isOfExpectationsType(IASTIdExpression idExpr) {
    return new CallsVectorTypeVerifier(idExpr).hasCallsVectorType();
  }

  private static boolean isOfRegistrationsType(IASTIdExpression idExpr) {
    return new CallsVectorTypeVerifier(idExpr).isVectorOfCallsVector();
  }

  private void markDiffsIfNecessary(Collection<MemFunCallExpectation> expectedCalls,
      IASTName toMark, Collection<ExistingMemFunCallRegistration> callRegistrations) {
    Collection<MemFunSignature> toRemove = orderPreservingDiff(expectedCalls, callRegistrations);
    Collection<MemFunSignature> toAdd = orderPreservingDiff(callRegistrations, expectedCalls);

    if (!toRemove.isEmpty() || !toAdd.isEmpty()) {
      mark(toMark, toRemove, toAdd);
    }
  }

  private static Collection<MemFunSignature> orderPreservingDiff(
      Collection<? extends MemFunSignature> setA, Collection<? extends MemFunSignature> setB) {
    Collection<MemFunSignature> diff = orderPreservingSet();

    for (MemFunSignature signature : setA) {
      if (!signature.isCovered(setB)) {
        diff.add(signature);
      }
    }
    return diff;
  }

  private Collection<ExistingMemFunCallRegistration> getCallRegistrations(IASTIdExpression vector) {
    try {
      IASTTranslationUnit ast = getModelCache().getAST();
      RegistrationCandidatesFinder finder = new RegistrationCandidatesFinder(ast, getCppStandard());
      return finder.findCallRegistrations(vector.getName());
    } catch (CoreException e) {
      throw new MockatorException(e);
    }
  }

  private CppStandard getCppStandard() {
    return CppStandard.fromCompilerFlags(getProject());
  }

  private void mark(IASTName toMark, Collection<MemFunSignature> toRemove,
      Collection<MemFunSignature> toAdd) {
    reportProblem(INCONSISTENT_EXPECTATIONS_PROBLEM_ID, toMark, getCodanArgs(toRemove, toAdd));
  }

  private static Object[] getCodanArgs(Collection<MemFunSignature> toRemove,
      Collection<MemFunSignature> toAdd) {
    return new ConsistentExpectationsCodanArgs(toRemove, toAdd).toArray();
  }

  private static Pair<Collection<MemFunCallExpectation>, IASTName> getExpectations(
      IASTFunctionDefinition function, IASTIdExpression expectedCalls) {
    ExpectationsFinder finder = new ExpectationsFinder(function);
    return finder.getExpectations(expectedCalls.getName());
  }

  private static Collection<ExpectedActualPair> getAssertedCalls(IASTFunctionDefinition function) {
    AssertEqualFinderVisitor visitor =
        new AssertEqualFinderVisitor(Maybe.<ICPPASTCompositeTypeSpecifier>none());
    function.accept(visitor);
    return visitor.getExpectedActual();
  }
}
