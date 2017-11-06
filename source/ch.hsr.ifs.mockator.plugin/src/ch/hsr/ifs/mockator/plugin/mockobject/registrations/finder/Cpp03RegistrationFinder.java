package ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;


public class Cpp03RegistrationFinder extends RegistrationFinder {

   @Override
   protected ExistingMemFunCallRegistration collectRegistration(final IASTInitializerClause pushBackArg) {
      final ICPPASTFunctionCallExpression funCall = getFunCall(pushBackArg);
      assureIsCallType(funCall);
      final IASTInitializerClause[] funArgs = funCall.getArguments();
      assureHasFunSignatureArg(funArgs);
      final IASTStatement containingStmt = getContainingStmt(pushBackArg);
      return toExistingCallRegistration(funArgs[0], containingStmt);
   }

   private static ICPPASTFunctionCallExpression getFunCall(final IASTInitializerClause pushBackArg) {
      Assert.instanceOf(pushBackArg, ICPPASTFunctionCallExpression.class, "Wrong call argument type");
      return (ICPPASTFunctionCallExpression) pushBackArg;
   }

   private void assureHasFunSignatureArg(final IASTInitializerClause[] funArgs) {
      Assert.isTrue(funArgs.length > 0, "A call must have arguments");
      Assert.isTrue(isStringLiteral(funArgs[0]), "Fun signature must be a string literal");
   }

   private static void assureIsCallType(final ICPPASTFunctionCallExpression funCall) {
      final IASTExpression funNameExpr = funCall.getFunctionNameExpression();
      Assert.isTrue(isCall(funNameExpr), "Not of call type");
   }

   private static boolean isCall(final IASTExpression functionNameExpression) {
      if (!(functionNameExpression instanceof IASTIdExpression)) return false;

      final IASTName name = ((IASTIdExpression) functionNameExpression).getName();
      return name.toString().equals(MockatorConstants.CALL);
   }
}
