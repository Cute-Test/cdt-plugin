package ch.hsr.ifs.cute.charwars.quickfixes.pointerparameter;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.utils.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.utils.analyzers.UEAnalyzer;

public class ReplaceInsideFunctionBodyVisitor extends ASTVisitor  {
	private ASTRewrite rewrite;
	private IASTName varName;

	public ReplaceInsideFunctionBodyVisitor(ASTRewrite rewrite, IASTName varName) {
		this.shouldVisitExpressions = true;
		this.rewrite = rewrite;
		this.varName = varName;
	}

	@Override
	public int leave(IASTExpression expression) {
		if(expression instanceof IASTIdExpression) {
			IASTIdExpression idExpression = (IASTIdExpression)expression;
			if(ASTAnalyzer.isSameName(idExpression.getName(), varName)) {
				IASTNode parent = idExpression.getParent();
				if(UEAnalyzer.isDereferenceExpression(parent)) {
					IASTIdExpression newIdExpression = ExtendedNodeFactory.newIdExpression(varName.toString());
					ASTModifier.replace(parent, newIdExpression, rewrite);
				}
				else if(parent instanceof IASTFieldReference) {
					IASTFieldReference oldFieldReference = (IASTFieldReference)parent;
					IASTFieldReference newFieldReference = ExtendedNodeFactory.newFieldReference(oldFieldReference.getFieldName().copy(), idExpression.copy());
					ASTModifier.replace(oldFieldReference, newFieldReference, rewrite);
				}
				else  {
					IASTUnaryExpression newUnaryExpression = ExtendedNodeFactory.newAdressOperatorExpression(idExpression.copy());
					ASTModifier.replace(idExpression, newUnaryExpression, rewrite);
				}
			}
		}
		return PROCESS_CONTINUE;
	}
}