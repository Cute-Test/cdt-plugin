package ch.hsr.ifs.cute.mockator.mockobject.registrations.finder;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;

import ch.hsr.ifs.cute.mockator.MockatorConstants;


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
        ILTISException.Unless.assignableFrom("Wrong call argument type", ICPPASTFunctionCallExpression.class, pushBackArg);
        return (ICPPASTFunctionCallExpression) pushBackArg;
    }

    private void assureHasFunSignatureArg(final IASTInitializerClause[] funArgs) {
        ILTISException.Unless.isTrue("A call must have arguments", funArgs.length > 0);
        ILTISException.Unless.isTrue("Fun signature must be a string literal", isStringLiteral(funArgs[0]));
    }

    private static void assureIsCallType(final ICPPASTFunctionCallExpression funCall) {
        final IASTExpression funNameExpr = funCall.getFunctionNameExpression();
        ILTISException.Unless.isTrue("Not of call type", isCall(funNameExpr));
    }

    private static boolean isCall(final IASTExpression functionNameExpression) {
        if (!(functionNameExpression instanceof IASTIdExpression)) return false;

        final IASTName name = ((IASTIdExpression) functionNameExpression).getName();
        return name.toString().equals(MockatorConstants.CALL);
    }
}
