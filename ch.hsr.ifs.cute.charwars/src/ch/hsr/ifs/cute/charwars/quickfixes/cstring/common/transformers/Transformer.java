package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers;

import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.ASTChangeDescription;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class Transformer {
	@SuppressFBWarnings(value="URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
	protected Context context = null;
	@SuppressFBWarnings(value="URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
	protected IASTIdExpression idExpression = null;
	protected IASTNode nodeToReplace = null;
	@SuppressFBWarnings(value="URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
	protected IASTName stringName = null;
	
	public Transformer(Context context, IASTIdExpression idExpression, IASTNode nodeToReplace) {
		this.context = context;
		this.idExpression = idExpression;
		this.nodeToReplace = nodeToReplace;
		
		if(this.idExpression != null) {
			this.stringName = idExpression.getName();
		}
	}
	
	public void transform(ASTChangeDescription changeDescription) {
		IASTNode replacementNode = getReplacementNode();
		changeDescription.setStatementHasChanged(true);
		ASTModifier.replaceNode(nodeToReplace, replacementNode);	
	}
	
	protected abstract IASTNode getReplacementNode();
}