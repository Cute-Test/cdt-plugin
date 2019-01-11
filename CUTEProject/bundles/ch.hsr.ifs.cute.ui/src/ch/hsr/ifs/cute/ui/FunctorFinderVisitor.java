package ch.hsr.ifs.cute.ui;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;


public class FunctorFinderVisitor extends ASTVisitor {

    private IASTDeclaration functor;

    public FunctorFinderVisitor() {
        functor = null;
    }

    {
        shouldVisitDeclarations = true;
    }

    public IASTDeclaration getFunctor() {
        return functor;
    }

    @Override
    public int visit(IASTDeclaration declaration) {
        if (declaration instanceof IASTFunctionDefinition) {
            IASTFunctionDefinition functionDefinition = (IASTFunctionDefinition) declaration;
            if (functionDefinition.getDeclarator().getName() instanceof ICPPASTOperatorName) {
                ICPPASTOperatorName operatorName = (ICPPASTOperatorName) functionDefinition.getDeclarator().getName();
                if (operatorName.toString().equals("operator ()")) {
                    functor = declaration;
                    return ASTVisitor.PROCESS_ABORT;
                }
            }
        }
        return ASTVisitor.PROCESS_CONTINUE;
    }

}
