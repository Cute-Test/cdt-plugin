package ch.hsr.ifs.cute.charwars.checkers;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.constants.Constants;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.cleanup.CStringCleanupQuickFix;

public class CStringCleanupChecker extends BaseChecker {
	public CStringCleanupChecker() {
		this.astVisitor = new CStringCleanupVisitor();
	}
	
	private class CStringCleanupVisitor extends ASTVisitor {
		public CStringCleanupVisitor() {
			shouldVisitExpressions = true;
		}
		
		@Override
		public int visit(IASTExpression expression) {
			for(String functionName : CStringCleanupQuickFix.functionMap.keySet()) {
				if(ASTAnalyzer.isCallToFunction(expression, functionName) || ASTAnalyzer.isCallToFunction(expression, Constants.STD_PREFIX + functionName)) {
					IASTNode parent = expression.getParent();
					while(parent instanceof IASTCastExpression) {
						parent = parent.getParent();
					}
					
					if(ASTAnalyzer.isAssignment(parent) || parent instanceof IASTEqualsInitializer) {
						IASTFunctionCallExpression functionCall = (IASTFunctionCallExpression)expression;
						IASTInitializerClause[] args = functionCall.getArguments();
						if(args.length > 0 && (ASTAnalyzer.isConversionToCharPointer(args[0], false) || ASTAnalyzer.isConversionToCharPointer(args[0], true))) {
							reportProblemForNode(ProblemIDs.C_STRING_CLEANUP_PROBLEM, functionCall, functionName);
							break;
						}
					}
				}	
			}
			return PROCESS_CONTINUE;
		}
	}
}
