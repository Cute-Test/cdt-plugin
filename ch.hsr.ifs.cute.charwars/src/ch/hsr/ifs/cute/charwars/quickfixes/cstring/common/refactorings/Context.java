package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.constants.StringType;
import ch.hsr.ifs.cute.charwars.utils.ExtendedNodeFactory;

public class Context {
	public enum Kind {
		Normal,
		Modified,
		Alias
	}
	
	private Kind kind;
	private String stringVarName;
	private String offsetVarName;
	private IASTStatement firstAffectedStatement;
	private StringType stringType;
	
	public Context(Kind kind, String stringVarName, String offsetVarName, IASTStatement firstAffectedStatement, StringType stringType) {
		this.kind = kind;
		this.stringVarName = stringVarName;
		this.offsetVarName = offsetVarName;
		this.firstAffectedStatement = firstAffectedStatement;
		this.stringType = stringType;
	}
	
	public boolean isOffset(IASTIdExpression idExpression) {
		switch(kind) {
		case Normal:
			return false;
		case Modified:
			IASTStatement topLevelStatement = ASTAnalyzer.getTopLevelParentStatement(idExpression.getOriginalNode());
			IASTCompoundStatement compoundStatement = (IASTCompoundStatement)topLevelStatement.getParent();
			List<IASTStatement> statements = Arrays.asList(compoundStatement.getStatements());
			int indexOfTopLevelStatement = statements.indexOf(topLevelStatement);
			int indexOfFirstAffectedStatement = statements.indexOf(firstAffectedStatement.getOriginalNode());
			return indexOfTopLevelStatement >= indexOfFirstAffectedStatement;
		case Alias:
			return true;
		default:
			return false;
		}
	}

	public Kind getKind() {
		return kind;
	}
	
	public IASTName createStringVarName() {
		return ExtendedNodeFactory.newName(stringVarName);
	}
	
	public IASTIdExpression createStringVarIdExpression() {
		return ExtendedNodeFactory.newIdExpression(stringVarName);
	}
	
	public IASTIdExpression createOffsetVarIdExpression() {
		return ExtendedNodeFactory.newIdExpression(offsetVarName);
	}
	
	public StringType getStringType() {
		return stringType;
	}
	
	public IASTNode getOffset(IASTIdExpression idExpression) {
		IASTNode offset = ASTModifier.transformToPointerOffset(idExpression);
		
		if(offset == null) {
			if(isOffset(idExpression)) {
				offset = createOffsetVarIdExpression();
			}
			else {
				offset = ExtendedNodeFactory.newIntegerLiteral(0);
			}
		}
		else if(isOffset(idExpression)) {
			offset = ExtendedNodeFactory.newPlusExpression(createOffsetVarIdExpression(), (IASTExpression)offset);
		}
		
		return offset;
	}
}
