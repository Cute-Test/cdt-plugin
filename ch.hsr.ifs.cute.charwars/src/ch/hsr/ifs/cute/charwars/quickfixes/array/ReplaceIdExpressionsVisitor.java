package ch.hsr.ifs.cute.charwars.quickfixes.array;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.constants.StdArray;
import ch.hsr.ifs.cute.charwars.utils.BEAnalyzer;

public class ReplaceIdExpressionsVisitor extends ASTVisitor {
	private ASTRewrite rewrite;
	private IASTName arrayName;

	public ReplaceIdExpressionsVisitor(ASTRewrite rewrite, IASTName arrayName) {
		this.shouldVisitExpressions = true;
		this.rewrite = rewrite;
		this.arrayName = arrayName;
	}

	@Override
	public int leave(IASTExpression expression) {
		if(expression instanceof IASTIdExpression) {
			IASTIdExpression idExpression = (IASTIdExpression) expression;
			if(ASTAnalyzer.isSameName(idExpression.getName(), arrayName)) {
				if(ASTAnalyzer.isArraySubscriptExpression(idExpression)) {
					// keep Array Subscript Expressions
				} 
				else if(ASTAnalyzer.isArrayLengthCalculation(idExpression)) {
					IASTNode currentNode = idExpression;
					while(currentNode != null) {
						currentNode = currentNode.getParent();
						if(BEAnalyzer.isDivision(currentNode))
							break;
					}
					IASTFunctionCallExpression sizeCall = ExtendedNodeFactory.newMemberFunctionCallExpression(arrayName, StdArray.SIZE);
					ASTModifier.replace(currentNode, sizeCall, rewrite);
				} 
				else {
					IASTFunctionCallExpression dataCall = ExtendedNodeFactory.newMemberFunctionCallExpression(arrayName, StdArray.DATA);
					ASTModifier.replace(idExpression, dataCall, rewrite);
				}
			}
		}
		return PROCESS_CONTINUE;
	}
}
