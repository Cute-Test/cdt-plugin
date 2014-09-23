package ch.hsr.ifs.cute.charwars.checkers;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.DeclaratorAnalyzer;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;

public class PointerParameterChecker extends BaseChecker {
	public PointerParameterChecker() {
		this.astVisitor = new PointerParameterVisitor();
	}
	
	private class PointerParameterVisitor extends ASTVisitor {
		public PointerParameterVisitor() {
			shouldVisitParameterDeclarations = true;
		}
		
		@Override
		public int visit(IASTParameterDeclaration parameterDeclaration) {
			IASTDeclarator declarator = parameterDeclaration.getDeclarator();
			
			if(DeclaratorAnalyzer.isPointer(declarator) && !DeclaratorAnalyzer.isArray(declarator)
				&& ASTAnalyzer.isFunctionDefinitionParameterDeclaration(parameterDeclaration) 
				&& !DeclaratorAnalyzer.hasCStringType(declarator, false)
				&& !DeclaratorAnalyzer.hasCStringType(declarator, true)) {
				reportProblemForDeclarator(ProblemIDs.POINTER_PARAMETER_PROBLEM, declarator);
			}
			return PROCESS_CONTINUE;
		}
	}
}
