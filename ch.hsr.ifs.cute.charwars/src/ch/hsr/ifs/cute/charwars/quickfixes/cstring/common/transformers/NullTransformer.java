package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers;

import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.ASTChangeDescription;

public class NullTransformer extends Transformer {
	public NullTransformer() {
		super(null, null, null);
	}
	
	@Override
	public void transform(ASTChangeDescription changeDescription) {
		//do nothing
	}
	
	@Override
	protected IASTNode getReplacementNode() {
		return null;
	}
}
