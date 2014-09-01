package ch.hsr.ifs.cute.charwars.asttools;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;

public class FindIdExpressionsVisitor extends ASTVisitor {
	private String name;
	private boolean foundIdExpression;
	
	public FindIdExpressionsVisitor(String name) {
		this.shouldVisitExpressions = true;
		this.shouldVisitDeclarators = true;
		this.name = name;
		this.foundIdExpression = false;
	}

	@Override
	public int leave(IASTExpression expression) {
		if(expression instanceof IASTIdExpression) {
			IASTIdExpression idExpression = (IASTIdExpression)expression;
			if(idExpression.getName().toString().equals(name)) {
				foundIdExpression = true;
				return PROCESS_ABORT;
			}
		}
		return PROCESS_CONTINUE;
	}
	
	@Override
	public int leave(IASTDeclarator declarator) {
		if(declarator.getName().toString().equals(name)) {
			foundIdExpression = true;
			return PROCESS_ABORT;
		}
		return PROCESS_CONTINUE;
	}
	
	public boolean hasFoundIdExpression() {
		return foundIdExpression;
	}
}
