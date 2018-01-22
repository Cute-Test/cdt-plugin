package ch.hsr.ifs.mockator.plugin.mockobject.support;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;

import ch.hsr.ifs.mockator.plugin.mockobject.asserteq.AssertEqualsInserter;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.reconcile.ExpectationsHandler;
import ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls.AllCallsVectorInserter;
import ch.hsr.ifs.mockator.plugin.mockobject.support.context.MockSupportContext;
import ch.hsr.ifs.mockator.plugin.testdouble.TestDoubleParentFinder;


public class MockSupportAdder {

   private final MockSupportContext context;

   public MockSupportAdder(final MockSupportContext context) {
      this.context = context;
   }

   public void addMockSupport() {
      createMockatorInclude();
      createMockatorInitCall();
      createAllCallsSequenceVector();
      addMockatorIdMember();
      addExpectations();
      addAssertEquals();
   }

   private void addAssertEquals() {
      new AssertEqualsInserter(context).insertAssertEqual();
   }

   private void addExpectations() {
      final ExpectationsHandler handler = new ExpectationsHandler(context);
      handler.addExpectations();
   }

   private void createMockatorInclude() {
      new MockatorIncludeInserter(context.getAst()).insertWith(context.getRewriter());
   }

   private void createMockatorInitCall() {
      final ICPPASTCompositeTypeSpecifier mockClass = context.getMockObject().getKlass();
      final IASTNode parent = new TestDoubleParentFinder(mockClass).getParentOfTestDouble();
      new MockatorInitCallInserter(mockClass, parent).insertWith(context.getRewriter());
   }

   private void createAllCallsSequenceVector() {
      new AllCallsVectorInserter(context.getMockObject().getKlass(), context.getMockObject().getParent(), context.getMockObject().getAllCallsVector(),
               context.getCppStandard()).insert(context.getRewriter());
   }

   private void addMockatorIdMember() {
      new MockIdFieldInserter(context.getInserter()).insert(context.getMockObject().hasMockIdField(), context.hasOnlyStaticMemFuns());
   }
}
