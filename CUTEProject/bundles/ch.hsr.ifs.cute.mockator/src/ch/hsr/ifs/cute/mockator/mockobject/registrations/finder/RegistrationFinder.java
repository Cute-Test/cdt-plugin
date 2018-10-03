package ch.hsr.ifs.cute.mockator.mockobject.registrations.finder;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.cute.mockator.MockatorConstants;
import ch.hsr.ifs.cute.mockator.refsupport.functions.params.StdString;
import ch.hsr.ifs.cute.mockator.testdouble.entities.ExistingTestDoubleMemFun;


abstract class RegistrationFinder {

    public Optional<ExistingMemFunCallRegistration> findRegistration(final IASTName callsVectorUsage) {
        if (!ASTUtil.isPushBack(callsVectorUsage) || !isArrayAccess(callsVectorUsage)) {
            return Optional.empty();
        }

        final ICPPASTFunctionCallExpression funCall = CPPVisitor.findAncestorWithType(callsVectorUsage, ICPPASTFunctionCallExpression.class).orElse(
                null);

        if (funCall == null || funCall.getArguments().length != 1) {
            return Optional.empty();
        }

        final IASTInitializerClause call = funCall.getArguments()[0];
        return Optional.of(collectRegistration(call));
    }

    protected abstract ExistingMemFunCallRegistration collectRegistration(IASTInitializerClause pushBackArg);

    private static boolean isArrayAccess(final IASTName callsVector) {
        return CPPVisitor.findAncestorWithType(callsVector, ICPPASTArraySubscriptExpression.class).orElse(null) != null;
    }

    protected ExistingMemFunCallRegistration toExistingCallRegistration(final IASTInitializerClause funSignature,
            final IASTStatement containingStmt) {
        final String signature = String.valueOf(((IASTLiteralExpression) funSignature).getValue());
        final ICPPASTFunctionDefinition parent = CPPVisitor.findAncestorWithType(containingStmt, ICPPASTFunctionDefinition.class).orElse(null);
        final ExistingTestDoubleMemFun memFun = new ExistingTestDoubleMemFun(parent);
        return new ExistingMemFunCallRegistration(memFun, containingStmt, signature);
    }

    protected boolean isNameCall(final IASTName name) {
        return name.toString().equals(MockatorConstants.CALL);
    }

    protected boolean isStringLiteral(final IASTInitializerClause param) {
        return new StdString().isStdString(param);
    }

    protected IASTStatement getContainingStmt(final IASTInitializerClause param) {
        return CPPVisitor.findAncestorWithType(param, IASTStatement.class).orElse(null);
    }
}
