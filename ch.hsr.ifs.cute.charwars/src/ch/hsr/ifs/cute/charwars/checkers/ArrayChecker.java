package ch.hsr.ifs.cute.charwars.checkers;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;

public class ArrayChecker extends BaseChecker {
	public ArrayChecker() {
		this.astVisitor = new ArrayVisitor();
	}
	
	private class ArrayVisitor extends ASTVisitor {
		public ArrayVisitor() {
			shouldVisitDeclarations = true;
		}
		
		@Override
		public int visit(IASTDeclaration decl) {
			if(decl instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration)decl;
				for(IASTDeclarator declarator : simpleDeclaration.getDeclarators()) {
					if(ASTAnalyzer.isArray(declarator)) {
						reportProblemForDeclarator(ProblemIDs.ARRAY_PROBLEM, declarator);
					}
				}
			}
			return PROCESS_CONTINUE;
		}
	}
}