package ch.hsr.ifs.cute.charwars.quickfixes.cstring.parameter;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.charwars.asttools.ASTRewriteCache;
import ch.hsr.ifs.cute.charwars.constants.ErrorMessages;
import ch.hsr.ifs.cute.charwars.constants.QuickFixLabels;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.quickfixes.BaseQuickFix;

public class CStringParameterQuickFix extends BaseQuickFix {
	private RewriteStrategy rewriteStrategy;
	
	@Override
	public String getLabel() {
		return QuickFixLabels.C_STRING_PARAMETER;
	}
	
	@Override
	protected String getErrorMessage() {
		return ErrorMessages.C_STRING_PARAMETER_QUICK_FIX;
	}
		
	@Override
	protected void handleMarkedNode(IASTNode markedNode, ASTRewrite rewrite, ASTRewriteCache rewriteCache) {
		ICPPASTParameterDeclaration parameterDeclaration = (ICPPASTParameterDeclaration)markedNode.getParent();
		rewriteStrategy = RewriteStrategyFactory.createRewriteStrategy(parameterDeclaration, rewrite);
		rewriteStrategy.addStdStringOverload();
		rewriteStrategy.adaptCStringOverload();
		rewriteStrategy.addNewDeclarations(rewriteCache);
		headers.add(StdString.HEADER_NAME);
	}
}