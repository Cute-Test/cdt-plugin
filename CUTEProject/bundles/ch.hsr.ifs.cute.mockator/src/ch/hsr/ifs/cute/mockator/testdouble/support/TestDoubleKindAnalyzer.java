package ch.hsr.ifs.cute.mockator.testdouble.support;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;

import ch.hsr.ifs.cute.mockator.mockobject.support.allcalls.CallsVectorTypeVerifier;
import ch.hsr.ifs.cute.mockator.refsupport.utils.NodeContainer;


public class TestDoubleKindAnalyzer {

   public enum TestDoubleKind {
      FakeObject, MockObject;
   }

   private final ICPPASTCompositeTypeSpecifier testDouble;

   public TestDoubleKindAnalyzer(final ICPPASTCompositeTypeSpecifier testDouble) {
      this.testDouble = testDouble;
   }

   public TestDoubleKind getKindOfTestDouble() {
      return usesVectorOfCalls() ? TestDoubleKind.MockObject : TestDoubleKind.FakeObject;
   }

   private boolean usesVectorOfCalls() {
      final NodeContainer<IASTIdExpression> callsVector = new NodeContainer<>();
      testDouble.accept(new ASTVisitor() {

         {
            shouldVisitExpressions = true;
         }

         @Override
         public int visit(final IASTExpression expr) {
            if (!(expr instanceof IASTIdExpression)) { return PROCESS_CONTINUE; }

            final IASTIdExpression idExpr = (IASTIdExpression) expr;
            final CallsVectorTypeVerifier verifier = new CallsVectorTypeVerifier(idExpr);

            if (verifier.isVectorOfCallsVector()) {
               callsVector.setNode(idExpr);
               return PROCESS_ABORT;
            }

            return PROCESS_CONTINUE;
         }
      });
      return callsVector.getNode().isPresent();
   }
}
