package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers;

import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.ASTChangeDescription;

public class RemoveStatementTransformer extends Transformer {
	public RemoveStatementTransformer() {
		super(null, null, null);
	}
	
	@Override
	public void transform(ASTChangeDescription changeDescription) {
		changeDescription.setRemoveStatement(true);
	}
	
	@Override
	protected IASTNode getReplacementNode() {
		return null;
	}
}
