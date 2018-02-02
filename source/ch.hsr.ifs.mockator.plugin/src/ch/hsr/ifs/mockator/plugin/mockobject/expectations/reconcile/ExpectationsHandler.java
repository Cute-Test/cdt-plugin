package ch.hsr.ifs.mockator.plugin.mockobject.expectations.reconcile;

import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder.ExistingMemFunCallRegistration;
import ch.hsr.ifs.mockator.plugin.mockobject.support.context.MockSupportContext;
import ch.hsr.ifs.mockator.plugin.testdouble.PolymorphismKind;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.DefaultConstructor;


public class ExpectationsHandler {

   private final MockSupportContext context;

   public ExpectationsHandler(final MockSupportContext context) {
      this.context = context;
   }

   public void addExpectations() {
      if (context.getNewForExpectations().isEmpty()) { return; }

      for (final ICPPASTFunctionDefinition testFun : context.getReferencingFunctions()) {
         if (findExpectationsVector(testFun).isPresent()) {
            reconcileExistingExpectations(testFun);
         } else {
            addNewExpectations(testFun);
         }
      }
   }

   private void addNewExpectations(final ICPPASTFunctionDefinition testFun) {
      final Set<TestDoubleMemFun> allTestDoubleMemFuns = new LinkedHashSet<>();
      allTestDoubleMemFuns.addAll(context.getMockObject().getRegisteredMemFuns(context.getCppStandard()));
      allTestDoubleMemFuns.addAll(context.getNewForExpectations());
      filterDefaultCtorIfNecessary(allTestDoubleMemFuns);
      insertNewExpectationVector(allTestDoubleMemFuns, testFun);
   }

   private void reconcileExistingExpectations(final ICPPASTFunctionDefinition testFun) {
      final List<ExistingMemFunCallRegistration> nothingToTemove = new ArrayList<>();
      reconcileExistingExpectations(testFun, context.getNewForExpectations(), nothingToTemove);
   }

   public void removeExpectation(final ExistingMemFunCallRegistration memFun, final IProgressMonitor pm) {
      for (final ICPPASTFunctionDefinition f : getReferencingTestFunctions(pm)) {
         final List<TestDoubleMemFun> nothingToAdd = new ArrayList<>();
         reconcileExistingExpectations(f, nothingToAdd, list(memFun));
      }
   }

   private void filterDefaultCtorIfNecessary(final Set<TestDoubleMemFun> allTestDoubleMemFuns) {
      if (context.getMockObject().getPolymorphismKind() != PolymorphismKind.SubTypePoly) { return; }

      allTestDoubleMemFuns.remove(new DefaultConstructor(context.getMockObject()));
   }

   private void insertNewExpectationVector(final Collection<? extends TestDoubleMemFun> newMemFunsForExpectations,
         final ICPPASTFunctionDefinition testFun) {
      new NewExpectationsInserter(testFun, context.getMockObject(), context.getCppStandard(), context.getRewriter(), context.getLinkedEditStrategy())
            .insertExpectations(newMemFunsForExpectations);
   }

   private void reconcileExistingExpectations(final ICPPASTFunctionDefinition testFun, final Collection<? extends TestDoubleMemFun> newMemFuns,
         final Collection<ExistingMemFunCallRegistration> toRemove) {
      findExpectationsVector(testFun).ifPresent((vector) -> createReconciler(testFun, vector).consolidateExpectations(newMemFuns, toRemove));
   }

   private ExpectationsReconciler createReconciler(final ICPPASTFunctionDefinition testFun, final IASTName vector) {
      return new ExpectationsReconciler(context.getRewriter(), vector, testFun, context.getCppStandard(), context.getLinkedEditStrategy());
   }

   private Optional<IASTName> findExpectationsVector(final ICPPASTFunctionDefinition testFun) {
      return new ExpectationsVectorDefinitionFinder(context.getMockObject(), testFun).findExpectationsVector();
   }

   private Collection<ICPPASTFunctionDefinition> getReferencingTestFunctions(final IProgressMonitor pm) {
      return context.getMockObject().getReferencingTestFunctions(context.getCRefContext(), context.getProject(), pm);
   }
}
