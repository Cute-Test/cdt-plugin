package ch.hsr.ifs.mockator.plugin.mockobject.expectations.reconcile;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._1;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._2;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.mockator.plugin.base.functional.F1;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.mockobject.MockObject;
import ch.hsr.ifs.mockator.plugin.mockobject.asserteq.AssertEqualFinderVisitor;
import ch.hsr.ifs.mockator.plugin.mockobject.asserteq.AssertKind.ExpectedActualPair;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.NameFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

class ExpectationsVectorDefinitionFinder {
  private final ICPPASTFunctionDefinition testFunction;
  private final MockObject mockObject;

  public ExpectationsVectorDefinitionFinder(MockObject mockObject, ICPPASTFunctionDefinition testFun) {
    this.mockObject = mockObject;
    this.testFunction = testFun;
  }

  public Maybe<IASTName> findExpectationsVector() {
    for (IASTName optRegVector : mockObject.getRegistrationVector()) {
      for (ExpectedActualPair expectedActual : findAssertedCallsInTestFunction()) {
        for (IASTName optExpVector : getExpectationsVector(expectedActual, optRegVector))
          return getNameOfDefinition(optExpVector);
      }
    }
    return none();
  }

  private Maybe<IASTName> getNameOfDefinition(final IASTName expectationsVector) {
    return new NameFinder(testFunction).getNameMatchingCriteria(new F1<IASTName, Boolean>() {
      @Override
      public Boolean apply(IASTName name) {
        return nameMatches(name) && isInDeclStmt(name);
      }

      private boolean nameMatches(IASTName name) {
        return name.toString().equals(expectationsVector.toString());
      }

      private boolean isInDeclStmt(IASTName name) {
        return AstUtil.getAncestorOfType(name, IASTDeclarationStatement.class) != null;
      }
    });
  }

  private static Maybe<IASTName> getExpectationsVector(ExpectedActualPair expectedActual,
      IASTName registrationVector) {
    IBinding vectorBinding = registrationVector.resolveBinding();
    IASTName candidate1 = _1(expectedActual).getName();
    IASTName candidate2 = _2(expectedActual).getName();

    if (vectorBinding.equals(candidate1.resolveBinding()))
      return maybe(candidate2);
    else if (vectorBinding.equals(candidate2.resolveBinding()))
      return maybe(candidate1);
    else
      return none();
  }

  private Collection<ExpectedActualPair> findAssertedCallsInTestFunction() {
    AssertEqualFinderVisitor visitor = new AssertEqualFinderVisitor(maybe(mockObject.getKlass()));
    testFunction.accept(visitor);
    return visitor.getExpectedActual();
  }
}
