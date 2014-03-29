package ch.hsr.ifs.mockator.plugin.mockobject.expectations.reconcile;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder.ExistingMemFunCallRegistration;
import ch.hsr.ifs.mockator.plugin.mockobject.support.context.MockSupportContext;
import ch.hsr.ifs.mockator.plugin.testdouble.PolymorphismKind;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.DefaultConstructor;

public class ExpectationsHandler {
  private final MockSupportContext context;

  public ExpectationsHandler(MockSupportContext context) {
    this.context = context;
  }

  public void addExpectations() {
    if (context.getNewForExpectations().isEmpty())
      return;

    for (ICPPASTFunctionDefinition testFun : context.getReferencingFunctions()) {
      if (findExpectationsVector(testFun).isSome()) {
        reconcileExistingExpectations(testFun);
      } else {
        addNewExpectations(testFun);
      }
    }
  }

  private void addNewExpectations(ICPPASTFunctionDefinition testFun) {
    Set<TestDoubleMemFun> allTestDoubleMemFuns = orderPreservingSet();
    allTestDoubleMemFuns.addAll(context.getMockObject().getRegisteredMemFuns(
        context.getCppStandard()));
    allTestDoubleMemFuns.addAll(context.getNewForExpectations());
    filterDefaultCtorIfNecessary(allTestDoubleMemFuns);
    insertNewExpectationVector(allTestDoubleMemFuns, testFun);
  }

  private void reconcileExistingExpectations(ICPPASTFunctionDefinition testFun) {
    List<ExistingMemFunCallRegistration> nothingToTemove = list();
    reconcileExistingExpectations(testFun, context.getNewForExpectations(), nothingToTemove);
  }

  public void removeExpectation(ExistingMemFunCallRegistration memFun, IProgressMonitor pm) {
    for (ICPPASTFunctionDefinition f : getReferencingTestFunctions(pm)) {
      List<TestDoubleMemFun> nothingToAdd = list();
      reconcileExistingExpectations(f, nothingToAdd, list(memFun));
    }
  }

  private void filterDefaultCtorIfNecessary(Set<TestDoubleMemFun> allTestDoubleMemFuns) {
    if (context.getMockObject().getPolymorphismKind() != PolymorphismKind.SubTypePoly)
      return;

    allTestDoubleMemFuns.remove(new DefaultConstructor(context.getMockObject()));
  }

  private void insertNewExpectationVector(
      Collection<? extends TestDoubleMemFun> newMemFunsForExpectations,
      ICPPASTFunctionDefinition testFun) {
    new NewExpectationsInserter(testFun, context.getMockObject(), context.getCppStandard(),
        context.getRewriter(), context.getLinkedEditStrategy())
        .insertExpectations(newMemFunsForExpectations);
  }

  private void reconcileExistingExpectations(ICPPASTFunctionDefinition testFun,
      Collection<? extends TestDoubleMemFun> newMemFuns,
      Collection<ExistingMemFunCallRegistration> toRemove) {
    for (IASTName optVector : findExpectationsVector(testFun)) {
      ExpectationsReconciler reconciler = createReconciler(testFun, optVector);
      reconciler.consolidateExpectations(newMemFuns, toRemove);
    }
  }

  private ExpectationsReconciler createReconciler(ICPPASTFunctionDefinition testFun, IASTName vector) {
    return new ExpectationsReconciler(context.getRewriter(), vector, testFun,
        context.getCppStandard(), context.getLinkedEditStrategy());
  }

  private Maybe<IASTName> findExpectationsVector(ICPPASTFunctionDefinition testFun) {
    return new ExpectationsVectorDefinitionFinder(context.getMockObject(), testFun)
        .findExpectationsVector();
  }

  private Collection<ICPPASTFunctionDefinition> getReferencingTestFunctions(IProgressMonitor pm) {
    return context.getMockObject().getReferencingTestFunctions(context.getCRefContext(),
        context.getProject(), pm);
  }
}
