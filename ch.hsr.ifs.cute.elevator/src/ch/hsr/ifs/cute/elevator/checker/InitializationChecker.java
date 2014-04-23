package ch.hsr.ifs.cute.elevator.checker;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;

import ch.hsr.ifs.cute.elevator.ast.DeclaratorCollector;
import ch.hsr.ifs.cute.elevator.ast.InitializerCollector;
import ch.hsr.ifs.cute.elevator.ast.NodeProperties;

public class InitializationChecker extends AbstractIndexAstChecker implements IChecker {

    public static String PROBLEM_ID = "ch.hsr.ifs.elevator.uniformInitialization";

    @Override
    public void processAst(IASTTranslationUnit ast) {
        collectAndReportDeclarators(ast);
        collectAndReportInitializers(ast);
    }

    private void collectAndReportInitializers(IASTTranslationUnit ast) {
        InitializerCollector initializerCollector = new InitializerCollector();
        ast.accept(initializerCollector);
        for (ICPPASTConstructorChainInitializer initializer : initializerCollector.getInitializers()) {
            reportProblem(PROBLEM_ID, initializer);
        }
    }

    private void collectAndReportDeclarators(IASTTranslationUnit ast) {
        DeclaratorCollector declaratorCollector = new DeclaratorCollector();
        ast.accept(declaratorCollector);
        for (IASTDeclarator declarator : declaratorCollector.getDeclarators()) {
            reportProblem(PROBLEM_ID, declarator);
        }
    }

    @Override
    public void reportProblem(String id, IASTNode astNode, Object... args) {
        NodeProperties nodeProperties = new NodeProperties(astNode);       
        if (nodeProperties.hasAncestor(ICPPASTNewExpression.class)) {
            astNode = nodeProperties.getAncestor(ICPPASTNewExpression.class);
        } 
        super.reportProblem(id, astNode, args);
    }
}
