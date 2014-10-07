package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.Context.ContextState;

public abstract class Refactoring {
	protected Map<String, Object> config = new HashMap<String, Object>();
	protected boolean isApplicable;
	protected static final String NODE_TO_REPLACE = "NODE_TO_REPLACE";
	protected Set<ContextState> contextStates;
	
	public boolean tryToApply(IASTIdExpression idExpression, Context context, ASTChangeDescription changeDescription) {
		clearConfiguration();
		prepareConfiguration(idExpression, context);
		
		if(isApplicable(context.getContextState())) {
			apply(idExpression, context);
			updateChangeDescription(changeDescription);
			return true;
		}
		return false;
	}
	
	protected void clearConfiguration() {
		config.clear();
		isApplicable = false;
	}
	
	protected abstract void prepareConfiguration(IASTIdExpression idExpression, Context context);
	
	protected void makeApplicable(IASTNode nodeToReplace) {
		isApplicable = true;
		config.put(NODE_TO_REPLACE, nodeToReplace);
	}
	
	protected boolean isApplicable(ContextState contextState) {
		return isApplicable && contextStates.contains(contextState);
	}
	
	protected void apply(IASTIdExpression idExpression, Context context) {
		IASTNode replacementNode = getReplacementNode(idExpression, context);
		if(replacementNode != null) {
			IASTNode nodeToReplace = (IASTNode)config.get(NODE_TO_REPLACE);
			ASTModifier.replaceNode(nodeToReplace, replacementNode);
		}
	}
	
	protected void updateChangeDescription(ASTChangeDescription changeDescription) {
		changeDescription.setStatementHasChanged(true);
	}
	
	protected IASTNode getReplacementNode(IASTIdExpression idExpression, Context context) {
		return null;
	}
	
	protected boolean canHandleOffsets() {
		return contextStates.contains(ContextState.CStringModified) || contextStates.contains(ContextState.CStringAlias);
	}
	
	protected void setContextStates(ContextState... contextStates) {
		this.contextStates = new HashSet<ContextState>();
		for(ContextState contextState : contextStates) {
			this.contextStates.add(contextState);
		}
	}
}