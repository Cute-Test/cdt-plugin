package ch.hsr.ifs.mockator.plugin.testdouble.support;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;

import ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls.CallsVectorTypeVerifier;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.NodeContainer;

public class TestDoubleKindAnalyzer {

  public enum TestDoubleKind {
    FakeObject, MockObject;
  }

  private final ICPPASTCompositeTypeSpecifier testDouble;

  public TestDoubleKindAnalyzer(ICPPASTCompositeTypeSpecifier testDouble) {
    this.testDouble = testDouble;
  }

  public TestDoubleKind getKindOfTestDouble() {
    return usesVectorOfCalls() ? TestDoubleKind.MockObject : TestDoubleKind.FakeObject;
  }

  private boolean usesVectorOfCalls() {
    final NodeContainer<IASTIdExpression> callsVector = new NodeContainer<IASTIdExpression>();
    testDouble.accept(new ASTVisitor() {
      {
        shouldVisitExpressions = true;
      }

      @Override
      public int visit(IASTExpression expr) {
        if (!(expr instanceof IASTIdExpression))
          return PROCESS_CONTINUE;

        IASTIdExpression idExpr = (IASTIdExpression) expr;
        CallsVectorTypeVerifier verifier = new CallsVectorTypeVerifier(idExpr);

        if (verifier.isVectorOfCallsVector()) {
          callsVector.setNode(idExpr);
          return PROCESS_ABORT;
        }

        return PROCESS_CONTINUE;
      }
    });
    return callsVector.getNode().isSome();
  }
}
