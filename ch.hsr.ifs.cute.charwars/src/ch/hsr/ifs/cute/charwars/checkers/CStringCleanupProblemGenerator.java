package ch.hsr.ifs.cute.charwars.checkers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.core.resources.IFile;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.constants.Function;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.cleanup.CStringCleanupQuickFix;
import ch.hsr.ifs.cute.charwars.utils.analyzers.BEAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.analyzers.FunctionAnalyzer;

public class CStringCleanupProblemGenerator {
	public static List<ProblemReport> generate(IFile file, IASTExpression expression) {
		List<ProblemReport> problemReports = new ArrayList<ProblemReport>();
		for(Function function : CStringCleanupQuickFix.functionMap.keySet()) {
			if(FunctionAnalyzer.isCallToFunction(expression, function)) {
				IASTNode parent = expression.getParent();
				while(parent instanceof IASTCastExpression) {
					parent = parent.getParent();
				}
				
				if(BEAnalyzer.isAssignment(parent) || parent instanceof IASTEqualsInitializer) {
					IASTFunctionCallExpression functionCall = (IASTFunctionCallExpression)expression;
					IASTInitializerClause[] args = functionCall.getArguments();
					if(args.length > 0 && ASTAnalyzer.isConversionToCharPointer(args[0])) {
						problemReports.add(new ProblemReport(file, ProblemIDs.C_STRING_CLEANUP_PROBLEM, functionCall, function.getName()));
						break;
					}
				}
			}	
		}
		return problemReports;
	}
}
