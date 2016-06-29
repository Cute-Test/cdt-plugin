package ch.hsr.ifs.cute.templator.plugin.asttools;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;

import java.util.ArrayList;
import java.util.List;

//TODO: Move to Formatting? Its only used there
public class FindFunctionCallExpressionsVisitor extends ASTVisitor {

	public FindFunctionCallExpressionsVisitor() {
		super(true);
	}

	private List<ICPPASTFunctionCallExpression> functionCalls = new ArrayList<>();

	@Override
	public int visit(IASTExpression expression) {

		if (expression instanceof ICPPASTFunctionCallExpression) {
			functionCalls.add((ICPPASTFunctionCallExpression) expression);
		}
		return super.visit(expression);
	}

	public List<ICPPASTFunctionCallExpression> getFunctionCalls() {
		return functionCalls;
	}
}