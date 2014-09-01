package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.constants.CString;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.mappings.Mapping;

public class OperatorTransformer extends Transformer {
	private Mapping mapping;
	
	public OperatorTransformer(Context context, IASTIdExpression idExpression, Mapping mapping) {
		super(context, idExpression, mapping.getInFunction().getName().equals(CString.STRCMP) ? ASTAnalyzer.getEnclosingBoolean(idExpression) : idExpression.getParent());
		this.mapping = mapping;
	}
	
	@Override
	protected IASTNode getReplacementNode() {
		IASTFunctionCallExpression functionCall = (IASTFunctionCallExpression)idExpression.getParent();
		IASTInitializerClause[] args = functionCall.getArguments();
		String outFunctionName = mapping.getOutFunction().getName();
		IASTExpression lhs = idExpression;
		
		if(outFunctionName.equals(StdString.OP_ASSIGNMENT)) {
			IASTExpression rhs = (IASTExpression)ASTAnalyzer.extractStdStringArg(args[1]);
			return ExtendedNodeFactory.newAssignment(lhs, rhs);
		}
		else if(outFunctionName.equals(StdString.OP_PLUS_ASSIGNMENT)) {
			IASTExpression rhs = (IASTExpression)ASTAnalyzer.extractStdStringArg(args[1]);
			return ExtendedNodeFactory.newPlusAssignment(lhs, rhs);
		}
		else if(outFunctionName.equals(StdString.OP_EQUALS)) {
			IASTExpression rhs = (IASTExpression)(args[0] == idExpression ? args[1] : args[0]);
			return ExtendedNodeFactory.newEqualityComparison(lhs, rhs, true);
		}
		else if(outFunctionName.equals(StdString.OP_NOT_EQUALS)) {
			IASTExpression rhs = (IASTExpression)(args[0] == idExpression ? args[1] : args[0]);
			return ExtendedNodeFactory.newEqualityComparison(lhs, rhs, false);	
		}
		return null;
	}
}
