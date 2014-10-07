package ch.hsr.ifs.cute.charwars.quickfixes.cstring.parameter;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;

import ch.hsr.ifs.cute.charwars.utils.ExtendedNodeFactory;

public class EmptyRewriteStrategy extends RewriteStrategy {
	@Override
	protected IASTCompoundStatement getStdStringOverloadBody() {
		return ExtendedNodeFactory.newCompoundStatement();
	}

	@Override
	public void adaptCStringOverload() {
	}
}
