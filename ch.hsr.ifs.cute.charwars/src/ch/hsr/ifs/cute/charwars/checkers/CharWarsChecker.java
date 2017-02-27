package ch.hsr.ifs.cute.charwars.checkers;

import java.util.List;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.core.resources.IFile;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;

public class CharWarsChecker extends AbstractIndexAstChecker {
	private CharWarsCheckerVisitor astVisitor = new CharWarsCheckerVisitor();
	
	private void reportProblems(List<ProblemReport> reports) {
		for(ProblemReport report : reports) {
			reportProblem(report);
		}
	}
	
	private void reportProblem(ProblemReport report) {
		String problemID = report.getProblemID();
		IProblemLocation problemLocation = report.getProblemLocation();
		Object[] args = report.getArgs();
		reportProblem(problemID, problemLocation, args);
	}
		
	private boolean isCppFile(IASTTranslationUnit ast) {
		return ast instanceof ICPPASTTranslationUnit;
	}
	
	@Override
	public void processAst(IASTTranslationUnit ast) {
		if(isCppFile(ast)) {
			astVisitor.updateProblemStates();
			ast.accept(astVisitor);
		}
	}
	
	private class CharWarsCheckerVisitor extends ASTVisitor {
		private IFile currentFile = null;
		private boolean cstringProblem = false;
		private boolean cstringAliasProblem = false;
		private boolean cstringCleanupProblem = false;
		private boolean cstrProblem = false;
		private boolean arrayProblem = false;
		private boolean pointerParameterProblem = false;
		private boolean cstringParameterProblem = false;
		
		public CharWarsCheckerVisitor() {
			shouldVisitExpressions = true;
			shouldVisitDeclarations = true;
			shouldVisitParameterDeclarations = true;
		}
		
		public void updateProblemStates() {
			currentFile = getFile();
			cstringProblem = getProblemById(ProblemIDs.C_STRING_PROBLEM, currentFile).isEnabled();
			cstringAliasProblem = getProblemById(ProblemIDs.C_STRING_ALIAS_PROBLEM, currentFile).isEnabled();
			cstringCleanupProblem = getProblemById(ProblemIDs.C_STRING_CLEANUP_PROBLEM, currentFile).isEnabled();
			cstrProblem = getProblemById(ProblemIDs.C_STR_PROBLEM, currentFile).isEnabled();
			arrayProblem = getProblemById(ProblemIDs.ARRAY_PROBLEM, currentFile).isEnabled();
			pointerParameterProblem = getProblemById(ProblemIDs.POINTER_PARAMETER_PROBLEM, currentFile).isEnabled();
			cstringParameterProblem = getProblemById(ProblemIDs.C_STRING_PARAMETER_PROBLEM, currentFile).isEnabled();
		}
		
		@Override
		public int visit(IASTExpression expression) {
			if(cstringCleanupProblem) {
				reportProblems(CStringCleanupProblemGenerator.generate(currentFile, expression));
			}
			
			if(cstrProblem) {
				reportProblems(CStrProblemGenerator.generate(currentFile, expression));
			}
			
			return PROCESS_CONTINUE;
		}
		
		@Override
		public int visit(IASTDeclaration decl) {
			if(cstringProblem) {
				reportProblems(CStringProblemGenerator.generate(currentFile, decl));
			}
			
			if(cstringAliasProblem) {
				reportProblems(CStringAliasProblemGenerator.generate(currentFile, decl));
			}
			
			if(arrayProblem) {
				reportProblems(ArrayProblemGenerator.generate(currentFile, decl));
			}
			
			return PROCESS_CONTINUE;
		}
		
		@Override
		public int visit(IASTParameterDeclaration parameterDeclaration) {
			if(cstringParameterProblem) {
				reportProblems(CStringParameterProblemGenerator.generate(currentFile, parameterDeclaration));
			}
			
			if(pointerParameterProblem) {
				reportProblems(PointerParameterProblemGenerator.generate(currentFile, parameterDeclaration));
			}
			
			return PROCESS_CONTINUE;
		}
	}
}
