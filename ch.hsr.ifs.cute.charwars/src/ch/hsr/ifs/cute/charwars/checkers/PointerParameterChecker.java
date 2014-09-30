package ch.hsr.ifs.cute.charwars.checkers;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;
import ch.hsr.ifs.cute.charwars.utils.DeclaratorTypeAnalyzer;

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
			
			if(DeclaratorTypeAnalyzer.isPointer(declarator) && !DeclaratorTypeAnalyzer.isArray(declarator)
				&& ASTAnalyzer.isFunctionDefinitionParameterDeclaration(parameterDeclaration) 
				&& !DeclaratorTypeAnalyzer.hasCStringType(declarator, false)
				&& !DeclaratorTypeAnalyzer.hasCStringType(declarator, true)) {
				reportProblemForDeclarator(ProblemIDs.POINTER_PARAMETER_PROBLEM, declarator);
			}
			return PROCESS_CONTINUE;
		}
	}
}
