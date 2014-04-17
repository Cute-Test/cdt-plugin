package ch.hsr.ifs.cute.elevator.ast;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;

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
        IASTDeclarator convertedDeclarator = declarator.copy();
        IASTInitializerList initList = declarator.getTranslationUnit().getASTNodeFactory().newInitializerList();
        IASTInitializer convertedInitializer = convertedDeclarator.getInitializer();
      
        if (convertedInitializer instanceof IASTEqualsInitializer) {
            IASTEqualsInitializer eqInitializer = (IASTEqualsInitializer) convertedInitializer;
            if (eqInitializer.getInitializerClause() instanceof ICPPASTFunctionCallExpression) {
                for (IASTInitializerClause clause :  ((ICPPASTFunctionCallExpression) eqInitializer.getInitializerClause()).getArguments()) {
                    initList.addClause(clause);
                }
            } else if (eqInitializer.getInitializerClause() instanceof ICPPASTSimpleTypeConstructorExpression){
                IASTInitializer initializerList = ((ICPPASTSimpleTypeConstructorExpression) eqInitializer.getInitializerClause()).getInitializer();
                if (initializerList instanceof IASTInitializerList) {
                    initList = (IASTInitializerList) initializerList.copy(CopyStyle.withoutLocations);
                }
            } else {
                initList.addClause(((IASTEqualsInitializer) convertedInitializer).getInitializerClause());
            }
        } else if (convertedInitializer instanceof ICPPASTConstructorInitializer) {
            for (IASTInitializerClause clause : ((ICPPASTConstructorInitializer) convertedInitializer).getArguments()) {
                initList.addClause(clause);
            }
        } else if (convertedInitializer instanceof ICPPASTInitializerList) {
            initList = (ICPPASTInitializerList) convertedInitializer;
        }
        convertedDeclarator.setInitializer(initList);
        return convertedDeclarator;
    }

}
