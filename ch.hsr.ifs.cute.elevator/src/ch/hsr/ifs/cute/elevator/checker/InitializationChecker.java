package ch.hsr.ifs.cute.elevator.checker;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;

import ch.hsr.ifs.cute.elevator.ast.analysis.DeclaratorCollector;
import ch.hsr.ifs.cute.elevator.ast.analysis.InitializerCollector;
import ch.hsr.ifs.cute.elevator.ast.analysis.NodeProperties;
import ch.hsr.ifs.cute.elevator.ast.analysis.conditions.Condition;
import ch.hsr.ifs.cute.elevator.ast.analysis.conditions.HasDefaultConstructor;

public class InitializationChecker extends AbstractIndexAstChecker implements IChecker {

    public static final String PROBLEM_ID = "ch.hsr.ifs.elevator.uniformInitialization";
    public static final String DEFAULT_CTOR = "ch.hsr.ifs.elevator.defaultConstructor";
    private final Condition hasDefaultConstructor = new HasDefaultConstructor();
    
    @Override
    public void processAst(final IASTTranslationUnit ast) {
        collectAndReportDeclarators(ast);
        collectAndReportInitializers(ast);
    }

    private void collectAndReportInitializers(final IASTTranslationUnit ast) {
        final InitializerCollector initializerCollector = new InitializerCollector();
        ast.accept(initializerCollector);
        for (IASTInitializer initializer : initializerCollector.getInitializers()) {
            reportProblem(initializer);
        }
    }

    private void collectAndReportDeclarators(final IASTTranslationUnit ast) {
        final DeclaratorCollector declaratorCollector = new DeclaratorCollector();
        ast.accept(declaratorCollector);
        for (IASTDeclarator declarator : declaratorCollector.getDeclarators()) {
            reportProblem(declarator);
        }
    }

    public void reportProblem(final IASTNode astNode, final Object... args) {
        final NodeProperties nodeProperties = new NodeProperties(astNode);
        final String id = hasDefaultConstructor.satifies(astNode) ? DEFAULT_CTOR : PROBLEM_ID;
        if (nodeProperties.hasAncestor(ICPPASTNewExpression.class)) {
            super.reportProblem(id, nodeProperties.getAncestor(ICPPASTNewExpression.class), args);
        } else {
            super.reportProblem(id, astNode, args);
        }
    }
}
