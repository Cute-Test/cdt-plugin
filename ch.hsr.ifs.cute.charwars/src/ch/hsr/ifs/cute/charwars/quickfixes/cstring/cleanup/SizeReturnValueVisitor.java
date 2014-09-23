package ch.hsr.ifs.cute.charwars.quickfixes.cstring.cleanup;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.constants.StringType;

public class SizeReturnValueVisitor extends ASTVisitor {
	private IASTName name;
	private ASTRewrite rewrite;
		
	public SizeReturnValueVisitor(IASTName name, ASTRewrite rewrite) {
		this.shouldVisitExpressions = true;
		this.name = name;
		this.rewrite = rewrite;
	}
		
	@Override
	public int leave(IASTExpression expression) {
		if(expression instanceof IASTIdExpression) {
			IASTIdExpression idExpression = (IASTIdExpression)expression;
			if(idExpression.getName().resolveBinding().equals(name.resolveBinding())) {
				handleSizeReturnType(idExpression);
			}
		}
		return PROCESS_CONTINUE;
	}
		
	private void handleSizeReturnType(IASTIdExpression idExpression) {
		if(ASTAnalyzer.isNodeComparedToStrlen(idExpression, true) ||
			ASTAnalyzer.isNodeComparedToStrlen(idExpression, false)) {
			IASTBinaryExpression comparison = (IASTBinaryExpression)idExpression.getParent();
			IASTExpression strlenCall = (idExpression == comparison.getOperand1()) ? comparison.getOperand2() : comparison.getOperand1();
			ASTModifier.replace(strlenCall, ExtendedNodeFactory.newNposExpression(StringType.STRING), rewrite);
		}
	}
}
