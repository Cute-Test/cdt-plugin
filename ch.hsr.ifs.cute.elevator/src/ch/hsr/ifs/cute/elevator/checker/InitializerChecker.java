package ch.hsr.ifs.cute.elevator.checker;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;

import ch.hsr.ifs.cute.elevator.ast.DeclaratorCollector;
import ch.hsr.ifs.cute.elevator.ast.InitializerCollector;


public class InitializerChecker extends AbstractIndexAstChecker implements IChecker {
	
	public static String PROBLEM_ID = "ch.hsr.ifs.elevator.initializationError";
	
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
	        reportProblem(PROBLEM_ID, getEclosingDeclaration(declarator));
	    }
    }

    private IASTNode getEclosingDeclaration(IASTNode node) {
        return node != null && node instanceof IASTDeclarator ? node : getEclosingDeclaration(node.getParent());
    }
}
