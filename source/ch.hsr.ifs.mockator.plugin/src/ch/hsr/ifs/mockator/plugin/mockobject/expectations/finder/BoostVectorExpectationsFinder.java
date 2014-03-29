package ch.hsr.ifs.mockator.plugin.mockobject.expectations.finder;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;
import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.filter;
import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.map;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.base.functional.F1;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.MemFunCallExpectation;
import ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls.CallsVectorTypeVerifier;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.NodeContainer;

// calls expectedMock;
// expectedMock += call("Mock()");
class BoostVectorExpectationsFinder extends AbstractExpectationsFinder {

  public BoostVectorExpectationsFinder(Collection<MemFunCallExpectation> callExpectations,
      NodeContainer<IASTName> expectationVector, IASTName expectationsVectorName) {
    super(callExpectations, expectationVector, expectationsVectorName);
  }

  @Override
  protected void collectExpectations(IASTStatement expectationStmt) {
    Assert.instanceOf(expectationStmt, IASTExpressionStatement.class,
        "Should be called with an expression statement");
    IASTExpression expression = ((IASTExpressionStatement) expectationStmt).getExpression();
    ICPPASTBinaryExpression binExpr = getBinaryExpr(expression);

    if (binExpr == null)
      return;

    IASTExpression operand1 = binExpr.getOperand1();

    if (!(operand1 instanceof IASTIdExpression))
      return;

    IASTIdExpression idExpr = (IASTIdExpression) operand1;

    if (!matchesName(idExpr.getName()) || !isTypeDefForCallsVector(idExpr))
      return;

    expectationVector.setNode(idExpr.getName());
    callExpectations.addAll(getMemFunCalls(expression));
  }

  private static boolean isTypeDefForCallsVector(IASTIdExpression idExpr) {
    return new CallsVectorTypeVerifier(idExpr).hasCallsVectorType();
  }

  private static ICPPASTBinaryExpression getBinaryExpr(IASTExpression expression) {
    return AstUtil.getChildOfType(expression, ICPPASTBinaryExpression.class);
  }

  private Collection<MemFunCallExpectation> getMemFunCalls(IASTExpression expression) {
    Collection<MemFunCallExpectation> expectations = orderPreservingSet();

    if (expression instanceof ICPPASTBinaryExpression) {
      collectSingleCallExpr(expression, expectations);
    } else if (expression instanceof IASTExpressionList) {
      collectCallsInExprList(expression, expectations);
    }

    return expectations;
  }

  private void collectSingleCallExpr(IASTExpression expression,
      Collection<MemFunCallExpectation> expectations) {
    ICPPASTBinaryExpression binExpr =
        AstUtil.getChildOfType(expression, ICPPASTBinaryExpression.class);
    IASTExpression operand2 = binExpr.getOperand2();

    if (isCallExpr(operand2)) {
      MemFunCallExpectation memFunCall = getMemFunCallIn(operand2);
      expectations.add(memFunCall);
    }
  }

  private void collectCallsInExprList(IASTExpression expression,
      Collection<MemFunCallExpectation> callExpectations) {
    Assert.instanceOf(expression, IASTExpressionList.class, "expression list expected");
    IASTExpression[] expressions = ((IASTExpressionList) expression).getExpressions();
    Collection<IASTExpression> onlyCalls = filterNonCallExpressions(expressions);
    toMemberFunctionCalls(callExpectations, onlyCalls);
  }

  private static Collection<IASTExpression> filterNonCallExpressions(IASTExpression[] expressions) {
    return filter(expressions, new F1<IASTExpression, Boolean>() {
      @Override
      public Boolean apply(IASTExpression param) {
        return isCallExpr(param);
      }
    });
  }

  private void toMemberFunctionCalls(Collection<MemFunCallExpectation> callExpectations,
      Collection<IASTExpression> onlyCalls) {
    Collection<MemFunCallExpectation> memFunCalls =
        map(onlyCalls, new F1<IASTExpression, MemFunCallExpectation>() {
          @Override
          public MemFunCallExpectation apply(IASTExpression param) {
            return getMemFunCallIn(param);
          }
        });
    for (MemFunCallExpectation call : memFunCalls) {
      callExpectations.add(call);
    }
  }

  private MemFunCallExpectation getMemFunCallIn(IASTExpression expression) {
    ICPPASTFunctionCallExpression funCall =
        AstUtil.getChildOfType(expression, ICPPASTFunctionCallExpression.class);
    Assert.notNull(funCall, "Function call exprected");
    IASTInitializerClause[] arguments = funCall.getArguments();
    Assert.isTrue(arguments.length > 0, "Call objects must have a fun signature");
    IASTInitializerClause funSignature = arguments[0];
    Assert.isTrue(isStringLiteral(funSignature), "Fun signature must be a string literal");
    return toMemberFunctionCall(funSignature);
  }

  private static boolean isCallExpr(IASTExpression expression) {
    ICPPASTFunctionCallExpression funCall =
        AstUtil.getChildOfType(expression, ICPPASTFunctionCallExpression.class);

    if (funCall == null)
      return false;

    IASTExpression functionNameExpr = funCall.getFunctionNameExpression();

    if (!(functionNameExpr instanceof IASTIdExpression))
      return false;

    return isNameCall(functionNameExpr);
  }

  private static boolean isNameCall(IASTExpression functionNameExpr) {
    return ((IASTIdExpression) functionNameExpr).getName().toString()
        .equals(MockatorConstants.CALL);
  }
}
