package ch.hsr.ifs.cute.elevator.ast;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;

/**
 * Converts a declarator to a C++11 initializer list.
 * 
 */

public class DeclaratorConverter {
    
    private final IASTDeclarator declarator;

    public DeclaratorConverter(IASTDeclarator declarator) {
        this.declarator = declarator;
    }

    public IASTDeclarator convert() {
        IASTDeclarator newDeclarator = declarator.copy();
        IASTInitializerList initList = declarator.getTranslationUnit().getASTNodeFactory().newInitializerList();
        IASTInitializer initializer = newDeclarator.getInitializer();
        if (new NodeProperties(declarator).hasAncestor(ICPPASTNewExpression.class)) {
            ICPPASTNewExpression expression = new NodeProperties(declarator).getAncestor(ICPPASTNewExpression.class);
             initializer = expression.getInitializer();
            for (IASTInitializerClause clause : ((ICPPASTConstructorInitializer) initializer).getArguments()) {
                initList.addClause(clause.copy());
            }
        } else if (initializer instanceof IASTEqualsInitializer) {
            initList.addClause(((IASTEqualsInitializer) initializer).getInitializerClause());
        } else if (initializer instanceof ICPPASTConstructorInitializer) {
            for (IASTInitializerClause clause : ((ICPPASTConstructorInitializer) initializer).getArguments()) {
                initList.addClause(clause);
            }
        } else if (initializer instanceof ICPPASTInitializerList) {
            initList = (ICPPASTInitializerList) initializer;
        }
        newDeclarator.setInitializer(initList);
        return newDeclarator;
    }

}
