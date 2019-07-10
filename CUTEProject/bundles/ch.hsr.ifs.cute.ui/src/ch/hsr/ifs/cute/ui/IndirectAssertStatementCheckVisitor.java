package ch.hsr.ifs.cute.ui;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;


public class IndirectAssertStatementCheckVisitor extends ASTVisitor {

    boolean hasIndirectAssertStmt = false;

    {
        shouldVisitStatements = true;
    }

    public boolean hasIndirectAssertStatement() {
        return hasIndirectAssertStmt;
    }

    @Override
    public int visit(IASTStatement statement) {
        if (statement instanceof IASTExpressionStatement) {
            IASTExpressionStatement exprStmt = (IASTExpressionStatement) statement;
            if (exprStmt.getExpression() instanceof IASTFunctionCallExpression) {
                IASTFunctionCallExpression funcCallExp = (IASTFunctionCallExpression) exprStmt.getExpression();
                if (funcCallExp.getFunctionNameExpression() instanceof IASTIdExpression) {
                    IASTIdExpression idExp = (IASTIdExpression) funcCallExp.getFunctionNameExpression();
                    IBinding binding = idExp.getName().resolveBinding();
                    if (binding instanceof ICPPInternalBinding) {
                        ICPPInternalBinding func = (ICPPInternalBinding) binding;
                        if (func.getDefinition().getParent() instanceof IASTFunctionDefinition) {
                            IASTFunctionDefinition funcDef = (IASTFunctionDefinition) func.getDefinition().getParent();
                            if (ASTUtil.containsAssert(funcDef)) {
                                hasIndirectAssertStmt = true;
                                return ASTVisitor.PROCESS_ABORT;
                            } else {
                                return ASTVisitor.PROCESS_CONTINUE;
                            }
                        }
                    }
                }
            }
        }
        return ASTVisitor.PROCESS_CONTINUE;
    }

}
