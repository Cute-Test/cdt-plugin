package ch.hsr.ifs.cute.charwars.quickfixes.cstring.cleanup;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.constants.Function;
import ch.hsr.ifs.cute.charwars.constants.StringType;

public class PtrReturnValueVisitor extends ASTVisitor {
	private IASTName name;
	private ASTRewrite rewrite;
	private String ptrVarName;
	private IASTIdExpression strNode;
		
	public PtrReturnValueVisitor(IASTName name, ASTRewrite rewrite, String ptrVarName, IASTIdExpression strNode) {
		this.shouldVisitExpressions = true;
		this.name = name;
		this.rewrite = rewrite;
		this.ptrVarName = ptrVarName;
		this.strNode = strNode;
	}
		
	@Override
	public int leave(IASTExpression expression) {
		if(expression instanceof IASTIdExpression) {
			IASTIdExpression idExpression = (IASTIdExpression)expression;
			if(idExpression.getName().resolveBinding().equals(name.resolveBinding())) {
				handlePtrReturnType(idExpression);
			}
		}
		return PROCESS_CONTINUE;
	}
		
	private void handlePtrReturnType(IASTIdExpression idExpression) {
		IASTIdExpression newIdExpression = ExtendedNodeFactory.newIdExpression(ptrVarName);
		IASTExpression npos = ExtendedNodeFactory.newNposExpression(StringType.STRING);
		IASTNode parent = idExpression.getParent();
		
		if(ASTAnalyzer.isCheckedIfNotEqualToNull(idExpression)) {
			IASTBinaryExpression comparison = ExtendedNodeFactory.newEqualityComparison(newIdExpression, npos, false);
			IASTNode node = idExpression;
			if(ASTAnalyzer.isInequalityCheck(parent)) {
				node = parent;
			}
			ASTModifier.replace(node, comparison, rewrite);
		}
		else if(ASTAnalyzer.isCheckedIfEqualToNull(idExpression)) {
			IASTBinaryExpression comparison = ExtendedNodeFactory.newEqualityComparison(newIdExpression, npos, true);
			ASTModifier.replace(parent, comparison, rewrite);
		}
		else if(isIndexCalculation(idExpression)) {
			ASTModifier.replace(parent, newIdExpression, rewrite);
		}
		else {
			IASTExpression ptrConversion = ExtendedNodeFactory.newAdressOperatorExpression(ExtendedNodeFactory.newArraySubscriptExpression(strNode, newIdExpression));
			ASTModifier.replace(idExpression, ptrConversion, rewrite);
		}
	}
	
	private boolean isIndexCalculation(IASTIdExpression idExpression) {
		IASTNode parent = idExpression.getParent();
		if(ASTAnalyzer.isSubtractionExpression(parent)) {
			IASTBinaryExpression subtraction = (IASTBinaryExpression)parent;
			if(subtraction.getOperand1() == idExpression && ASTAnalyzer.isCallToMemberFunction(subtraction.getOperand2(), Function.C_STR)) {
				return true;
			}
		}
		return false;
	}
}
