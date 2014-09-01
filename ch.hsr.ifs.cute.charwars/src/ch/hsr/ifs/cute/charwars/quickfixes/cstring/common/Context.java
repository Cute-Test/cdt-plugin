package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common;

import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;

public class Context {
	private boolean modifiesCharPointer;
	private IASTStatement firstAffectedStatement;
	private String posVariableName;
	
	public Context() {
		this.modifiesCharPointer = false;
		this.firstAffectedStatement = null;
		this.posVariableName = null;
	}
	
	public Context(IASTStatement firstAffectedStatement, String posVariableName) {
		this.modifiesCharPointer = true;
		this.firstAffectedStatement = firstAffectedStatement;
		this.posVariableName = posVariableName;
	}
	
	public boolean isPotentiallyModifiedCharPointer(IASTIdExpression idExpression) {
		if(!this.modifiesCharPointer) {
			return false;
		}
		
		IASTStatement topLevelStatement = ASTAnalyzer.getTopLevelParentStatement(idExpression.getOriginalNode());
		IASTCompoundStatement compoundStatement = (IASTCompoundStatement)topLevelStatement.getParent();
		List<IASTStatement> statements = Arrays.asList(compoundStatement.getStatements());
		int indexOfTopLevelStatement = statements.indexOf(topLevelStatement);
		int indexOfFirstAffectedStatement = statements.indexOf(firstAffectedStatement);
		return indexOfTopLevelStatement >= indexOfFirstAffectedStatement;
	}
	
	public IASTStatement getFirstAffectedStatement() {
		return this.firstAffectedStatement;
	}
	
	public String getPosVariableName() {
		return this.posVariableName;
	}
}
