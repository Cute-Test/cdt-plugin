package ch.hsr.ifs.mockator.plugin.mockobject.expectations.qf;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._1;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._2;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.mockobject.asserteq.AssertEqualFinderVisitor;
import ch.hsr.ifs.mockator.plugin.mockobject.asserteq.AssertKind.ExpectedActualPair;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.reconcile.ExpectationsReconciler;
import ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder.ExistingMemFunCallRegistration;
import ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls.CallsVectorTypeVerifier;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.ExistingTestDoubleMemFun;

@SuppressWarnings("restriction")
class ConsistentExpectationsRefactoring extends MockatorRefactoring {
  private final ConsistentExpectationsCodanArgs ca;
  private final LinkedEditModeStrategy linkedEditMode;
  private final CppStandard cppStd;
  private final List<ExistingTestDoubleMemFun> expectationsToAdd;

  public ConsistentExpectationsRefactoring(ICElement cElement, ITextSelection selection,
      ICProject project, ConsistentExpectationsCodanArgs ca, CppStandard cppStd,
      LinkedEditModeStrategy linkedEditMode) {
    super(cElement, selection, project);
    this.ca = ca;
    this.cppStd = cppStd;
    this.linkedEditMode = linkedEditMode;
    expectationsToAdd = list();
  }

  @Override
  public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
    RefactoringStatus status = super.checkInitialConditions(pm);
    Maybe<IASTName> expectationsVector = getSelectedName(getAST(tu, pm));

    if (expectationsVector.isNone()) {
      status.addFatalError("Not a valid name selected");
    } else if (!isOfCallsVectorType(expectationsVector)) {
      status.addFatalError("Not a valid calls vector selected");
    }
    return status;
  }

  private static boolean isOfCallsVectorType(Maybe<IASTName> expectationsVector) {
    return new CallsVectorTypeVerifier(expectationsVector.get()).hasCallsVectorType();
  }

  @Override
  protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
      throws CoreException, OperationCanceledException {
    IASTTranslationUnit ast = getAST(tu, pm);

    for (IASTName optExpectations : getSelectedName(ast)) {
      reconcileExpectations(collector, ast, optExpectations);
    }
  }

  private void reconcileExpectations(ModificationCollector collector, IASTTranslationUnit ast,
      IASTName expectationsVector) {
    ASTRewrite rewriter = createRewriter(collector, ast);
    collectExpectationsToAdd(ast, expectationsVector);
    consolidateExpectations(expectationsVector, rewriter);
  }

  private void consolidateExpectations(IASTName expectationsVector, ASTRewrite rewriter) {
    ExpectationsReconciler reconciler =
        new ExpectationsReconciler(rewriter, expectationsVector,
            getTestFunction(expectationsVector), cppStd, linkedEditMode);
    reconciler.consolidateExpectations(expectationsToAdd, ca.getExpectationsToRemove());
  }

  private void collectExpectationsToAdd(IASTTranslationUnit ast, IASTName expectationsVector) {
    for (ExistingMemFunCallRegistration registration : collectRegisteredCalls(expectationsVector,
        ast)) {
      if (ca.getExpectationsToAdd().contains(registration.getMemFunSignature())) {
        expectationsToAdd.add(registration.getExistingMemFun());
      }
    }
  }

  private Collection<ExistingMemFunCallRegistration> collectRegisteredCalls(
      IASTName expectationsVector, IASTTranslationUnit ast) {
    RegistrationCandidatesFinder finder = new RegistrationCandidatesFinder(ast, cppStd);
    ICPPASTFunctionDefinition testFunction = getTestFunction(expectationsVector);

    for (IASTName optRegistrationVector : getRegistrationVector(testFunction, expectationsVector))
      return finder.findCallRegistrations(optRegistrationVector);

    return list();
  }

  private static Maybe<IASTName> getRegistrationVector(ICPPASTFunctionDefinition testFun,
      IASTName expectationsVector) {
    for (ExpectedActualPair expectedActual : getAssertedCalls(testFun)) {
      if (equalsName(_1(expectedActual), expectationsVector))
        return maybe(_2(expectedActual).getName());

      if (equalsName(_2(expectedActual), expectationsVector))
        return maybe(_1(expectedActual).getName());
    }

    return none();
  }

  private static boolean equalsName(IASTIdExpression idExpr, IASTName name) {
    return Arrays.equals(idExpr.getName().toCharArray(), name.toCharArray());
  }

  private static Collection<ExpectedActualPair> getAssertedCalls(IASTFunctionDefinition function) {
    AssertEqualFinderVisitor visitor =
        new AssertEqualFinderVisitor(Maybe.<ICPPASTCompositeTypeSpecifier>none());
    function.accept(visitor);
    return visitor.getExpectedActual();
  }

  private static ICPPASTFunctionDefinition getTestFunction(IASTName expectationsVector) {
    return AstUtil.getAncestorOfType(expectationsVector, ICPPASTFunctionDefinition.class);
  }

  @Override
  public String getDescription() {
    return I18N.ConsistentExpectationsRefactoringDesc;
  }

  Collection<ExistingTestDoubleMemFun> getExpectationsToAdd() {
    return expectationsToAdd;
  }
}
