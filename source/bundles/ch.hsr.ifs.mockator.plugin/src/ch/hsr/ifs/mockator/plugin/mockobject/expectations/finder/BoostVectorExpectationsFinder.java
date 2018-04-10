package ch.hsr.ifs.mockator.plugin.mockobject.expectations.finder;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;

import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.iltis.cpp.wrappers.CPPVisitor;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.MemFunCallExpectation;
import ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls.CallsVectorTypeVerifier;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.NodeContainer;


// calls expectedMock;
// expectedMock += call("Mock()");
class BoostVectorExpectationsFinder extends AbstractExpectationsFinder {

   public BoostVectorExpectationsFinder(final Collection<MemFunCallExpectation> callExpectations, final NodeContainer<IASTName> expectationVector,
                                        final IASTName expectationsVectorName) {
      super(callExpectations, expectationVector, expectationsVectorName);
   }

   @Override
   protected void collectExpectations(final IASTStatement expectationStmt) {
      ILTISException.Unless.assignableFrom("Should be called with an expression statement", IASTExpressionStatement.class, expectationStmt);
      final IASTExpression expression = ((IASTExpressionStatement) expectationStmt).getExpression();
      final ICPPASTBinaryExpression binExpr = getBinaryExpr(expression);

      if (binExpr == null) { return; }

      final IASTExpression operand1 = binExpr.getOperand1();

      if (!(operand1 instanceof IASTIdExpression)) { return; }

      final IASTIdExpression idExpr = (IASTIdExpression) operand1;

      if (!matchesName(idExpr.getName()) || !isTypeDefForCallsVector(idExpr)) { return; }

      expectationVector.setNode(idExpr.getName());
      callExpectations.addAll(getMemFunCalls(expression));
   }

   private static boolean isTypeDefForCallsVector(final IASTIdExpression idExpr) {
      return new CallsVectorTypeVerifier(idExpr).hasCallsVectorType();
   }

   private static ICPPASTBinaryExpression getBinaryExpr(final IASTExpression expression) {
      return CPPVisitor.findChildWithType(expression, ICPPASTBinaryExpression.class).orElse(null);
   }

   private Collection<MemFunCallExpectation> getMemFunCalls(final IASTExpression expression) {
      final Collection<MemFunCallExpectation> expectations = new LinkedHashSet<>();

      if (expression instanceof ICPPASTBinaryExpression) {
         collectSingleCallExpr(expression, expectations);
      } else if (expression instanceof IASTExpressionList) {
         collectCallsInExprList(expression, expectations);
      }

      return expectations;
   }

   private void collectSingleCallExpr(final IASTExpression expression, final Collection<MemFunCallExpectation> expectations) {
      final ICPPASTBinaryExpression binExpr = CPPVisitor.findChildWithType(expression, ICPPASTBinaryExpression.class).orElse(null);
      final IASTExpression operand2 = binExpr.getOperand2();

      if (isCallExpr(operand2)) {
         final MemFunCallExpectation memFunCall = getMemFunCallIn(operand2);
         expectations.add(memFunCall);
      }
   }

   private void collectCallsInExprList(final IASTExpression expression, final Collection<MemFunCallExpectation> callExpectations) {
      ILTISException.Unless.assignableFrom("expression list expected", IASTExpressionList.class, expression);
      final IASTExpression[] expressions = ((IASTExpressionList) expression).getExpressions();
      final Collection<IASTExpression> onlyCalls = filterNonCallExpressions(expressions);
      toMemberFunctionCalls(callExpectations, onlyCalls);
   }

   private static Collection<IASTExpression> filterNonCallExpressions(final IASTExpression[] expressions) {
      return Arrays.asList(expressions).stream().filter((param) -> isCallExpr(param)).collect(Collectors.toList());
   }

   private void toMemberFunctionCalls(final Collection<MemFunCallExpectation> callExpectations, final Collection<IASTExpression> onlyCalls) {
      final Collection<MemFunCallExpectation> memFunCalls = onlyCalls.stream().map((param) -> getMemFunCallIn(param)).collect(Collectors.toList());
      for (final MemFunCallExpectation call : memFunCalls) {
         callExpectations.add(call);
      }
   }

   private MemFunCallExpectation getMemFunCallIn(final IASTExpression expression) {
      final ICPPASTFunctionCallExpression funCall = CPPVisitor.findChildWithType(expression, ICPPASTFunctionCallExpression.class).orElse(null);
      ILTISException.Unless.notNull("Function call exprected", funCall);
      final IASTInitializerClause[] arguments = funCall.getArguments();
      ILTISException.Unless.isTrue("Call objects must have a fun signature", arguments.length > 0);
      final IASTInitializerClause funSignature = arguments[0];
      ILTISException.Unless.isTrue("Fun signature must be a string literal", isStringLiteral(funSignature));
      return toMemberFunctionCall(funSignature);
   }

   private static boolean isCallExpr(final IASTExpression expression) {
      final ICPPASTFunctionCallExpression funCall = CPPVisitor.findChildWithType(expression, ICPPASTFunctionCallExpression.class).orElse(null);

      if (funCall == null) { return false; }

      final IASTExpression functionNameExpr = funCall.getFunctionNameExpression();

      if (!(functionNameExpr instanceof IASTIdExpression)) { return false; }

      return isNameCall(functionNameExpr);
   }

   private static boolean isNameCall(final IASTExpression functionNameExpr) {
      return ((IASTIdExpression) functionNameExpr).getName().toString().equals(MockatorConstants.CALL);
   }
}
