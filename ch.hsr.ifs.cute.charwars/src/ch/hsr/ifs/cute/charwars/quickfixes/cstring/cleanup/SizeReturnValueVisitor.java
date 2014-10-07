package ch.hsr.ifs.cute.charwars.quickfixes.cstring.cleanup;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.asttools.CheckAnalyzer;
import ch.hsr.ifs.cute.charwars.constants.StringType;
import ch.hsr.ifs.cute.charwars.utils.BEAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.ExtendedNodeFactory;

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
			if(ASTAnalyzer.isSameName(idExpression.getName(), name)) {
				handleSizeReturnType(idExpression);
			}
		}
		return PROCESS_CONTINUE;
	}
		
	private void handleSizeReturnType(IASTIdExpression idExpression) {
		if(CheckAnalyzer.isNodeComparedToStrlen(idExpression)) {
			IASTExpression strlenCall = BEAnalyzer.getOtherOperand(idExpression);
			ASTModifier.replace(strlenCall, ExtendedNodeFactory.newNposExpression(StringType.STRING), rewrite);
		}
	}
}
