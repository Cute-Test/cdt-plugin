package ch.hsr.ifs.cute.charwars.quickfixes.cstr;

import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.asttools.ASTRewriteCache;
import ch.hsr.ifs.cute.charwars.constants.ErrorMessages;
import ch.hsr.ifs.cute.charwars.constants.QuickFixLabels;
import ch.hsr.ifs.cute.charwars.quickfixes.BaseQuickFix;

public class CStrQuickFix extends BaseQuickFix {
	@Override
	public String getLabel() {
		String functionSignature = getProblemArgument(currentMarker, 0);
		return QuickFixLabels.C_STR + functionSignature;
	}
	
	@Override
	protected String getErrorMessage() {
		return ErrorMessages.C_STR_QUICK_FIX;
	}
	
	@Override
	protected void handleMarkedNode(IASTNode markedNode, ASTRewriteCache rewriteCache) {
		IASTFunctionCallExpression cStrCall = (IASTFunctionCallExpression)markedNode;
		IASTNode stdString = ASTAnalyzer.extractStdStringArg(cStrCall);
		ASTRewrite rewrite = getRewrite(rewriteCache, markedNode);
		ASTModifier.replace(cStrCall, stdString, rewrite);
	}
}
