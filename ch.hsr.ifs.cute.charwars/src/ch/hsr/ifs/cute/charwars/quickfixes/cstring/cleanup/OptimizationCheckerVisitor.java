package ch.hsr.ifs.cute.charwars.quickfixes.cstring.cleanup;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;

public class OptimizationCheckerVisitor extends ASTVisitor {
	private IASTName name;
	boolean ptrReturnType;
	private List<IASTIdExpression> idExpressions;
	private List<IASTNode> inequalityChecks;
	
	public OptimizationCheckerVisitor(IASTName name, boolean ptrReturnType) {
		this.shouldVisitExpressions = true;
		this.shouldVisitDeclarators = true;
		this.name = name;
		this.ptrReturnType = ptrReturnType;
		this.idExpressions = new ArrayList<IASTIdExpression>();
		this.inequalityChecks = new ArrayList<IASTNode>();
	}

	@Override
	public int leave(IASTExpression expression) {
		if(expression instanceof IASTIdExpression) {
			IASTIdExpression idExpression = (IASTIdExpression)expression;
			if(idExpression.getName().resolveBinding().equals(name.resolveBinding())) {
				handleIdExpression(idExpression);
			}
		}
		return PROCESS_CONTINUE;
	}
	
	private void handleIdExpression(IASTIdExpression idExpression) {
		IASTNode parent = idExpression.getParent().getParent();
		
		if(ptrReturnType) {
			if(ASTAnalyzer.isCheckedIfNotEqualToNull(idExpression)) {
				inequalityChecks.add(parent);
			}
			else if(ASTAnalyzer.isCheckedIfEqualToNull(idExpression)) {
				//ignore
			}
			else {
				idExpressions.add(idExpression);
			}
		}
		else {
			if(ASTAnalyzer.isNodeComparedToStrlen(idExpression, false) && parent instanceof IASTIfStatement) {
				inequalityChecks.add(parent);
			}
			else if(ASTAnalyzer.isNodeComparedToStrlen(idExpression, true) && parent instanceof IASTIfStatement) {
				//ignore
			}
			else {
				idExpressions.add(idExpression);
			}
		}
	}
	
	public boolean isOptimizationPossible() {
		for(IASTIdExpression idExpression : idExpressions) {
			boolean isChild = false;
			for(IASTNode check : inequalityChecks) {
				if(check.contains(idExpression)) {
					isChild = true;
					break;
				}
			}
			if(!isChild) {
				return false;
			}
		}
		return true;
	}
}
