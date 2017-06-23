package ch.hsr.ifs.cute.charwars.quickfixes.cstring.parameter;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;

import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.asttools.IndexFinder;
import ch.hsr.ifs.cute.charwars.asttools.IndexFinder.IndexFinderInstruction;
import ch.hsr.ifs.cute.charwars.utils.ExtendedNodeFactory;

public class NoNullCheckRewriteStrategy extends RewriteStrategy {
	@Override
	protected IASTCompoundStatement getStdStringOverloadBody() {
		final IASTCompoundStatement body = ExtendedNodeFactory.newCompoundStatement();
		for(final IASTStatement statement : statements) {
			body.addStatement(statement.copy(CopyStyle.withLocations));
		}
		return body;
	}

	@Override
	public void adaptCStringOverload() {
		ASTModifier.remove(functionDefinition, getMainRewrite());
		IndexFinder.findDeclarations(functionDefinition.getDeclarator().getName(), rewriteCache, (name, rewrite) -> {
			final ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator)name.getParent();
			if(functionDeclarator.getParent() instanceof IASTSimpleDeclaration) {
				ASTModifier.remove(functionDeclarator.getParent(), rewrite);
			}
			return IndexFinderInstruction.CONTINUE_SEARCH;
		});
	}

	@Override
	protected boolean shouldCopyDefaultValueOfParameter() {
		return true;
	}
}
