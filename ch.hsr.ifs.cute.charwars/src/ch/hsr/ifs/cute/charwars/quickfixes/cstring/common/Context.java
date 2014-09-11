package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common;

import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;

public class Context {
	public enum ContextState {
		CString,
		CStringModified,
		CStringAlias
	}
	
	private ContextState contextState;
	private String stringVarName;
	private String offsetVarName;
	private IASTStatement firstAffectedStatement;
	
	public Context(ContextState contextState, String stringVarName, String offsetVarName, IASTStatement firstAffectedStatement) {
		this.contextState = contextState;
		this.stringVarName = stringVarName;
		this.offsetVarName = offsetVarName;
		this.firstAffectedStatement = firstAffectedStatement;
	}
	
	public boolean isOffset(IASTIdExpression idExpression) {
		switch(contextState) {
		case CString:
			return false;
		case CStringModified:
			IASTStatement topLevelStatement = ASTAnalyzer.getTopLevelParentStatement(idExpression.getOriginalNode());
			IASTCompoundStatement compoundStatement = (IASTCompoundStatement)topLevelStatement.getParent();
			List<IASTStatement> statements = Arrays.asList(compoundStatement.getStatements());
			int indexOfTopLevelStatement = statements.indexOf(topLevelStatement);
			int indexOfFirstAffectedStatement = statements.indexOf(firstAffectedStatement.getOriginalNode());
			return indexOfTopLevelStatement >= indexOfFirstAffectedStatement;
		case CStringAlias:
			return true;
		default:
			return false;
		}
	}

	public ContextState getContextState() {
		return contextState;
	}
	
	public String getStringVarName() {
		return stringVarName;
	}
	
	public String getOffsetVarName() {
		return offsetVarName;
	}
	
	public IASTIdExpression createOffsetVarIdExpression() {
		return ExtendedNodeFactory.newIdExpression(offsetVarName);
	}
}
