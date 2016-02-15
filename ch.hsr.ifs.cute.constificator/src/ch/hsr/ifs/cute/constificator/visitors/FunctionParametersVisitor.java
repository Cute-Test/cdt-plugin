package ch.hsr.ifs.cute.constificator.visitors;

import static ch.hsr.ifs.cute.constificator.core.util.ast.Relation.*;
import static ch.hsr.ifs.cute.constificator.core.util.type.Cast.*;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import ch.hsr.ifs.cute.constificator.checkers.FunctionParametersChecker;
import ch.hsr.ifs.cute.constificator.core.deciders.decision.IDecision;
import ch.hsr.ifs.cute.constificator.core.deciders.functionparameters.NonPointerParameter;
import ch.hsr.ifs.cute.constificator.core.deciders.functionparameters.PointerParameter;

public class FunctionParametersVisitor extends ASTVisitor {

	private final FunctionParametersChecker parent;

	public FunctionParametersVisitor(FunctionParametersChecker parent) {
		shouldVisitParameterDeclarations = true;
		this.parent = parent;
	}

	@Override
	public int visit(IASTParameterDeclaration parameterDeclaration) {
		ICPPASTParameterDeclaration cppParameterDeclaration = as(ICPPASTParameterDeclaration.class,
				parameterDeclaration);

		if (cppParameterDeclaration == null) {
			return PROCESS_SKIP;
		}

		ICPPASTFunctionDeclarator functionDeclarator = as(ICPPASTFunctionDeclarator.class,
				cppParameterDeclaration.getParent());

		if(isDescendendOf(ICPPASTTemplateDeclaration.class, functionDeclarator)) {
			return PROCESS_SKIP;
		}

		if (functionDeclarator.getName() != null && functionDeclarator.getName().toString().equals("main")) {
			return PROCESS_SKIP;
		}

		if (getGrandparentOf(ICPPASTFunctionDefinition.class, parameterDeclaration) == null && getGrandparentOf(ICPPASTLambdaExpression.class, parameterDeclaration) == null) {
			return PROCESS_SKIP;
		}

		ICPPASTDeclarator cppDeclarator = as(ICPPASTDeclarator.class, cppParameterDeclaration.getDeclarator());

		if (cppDeclarator == null) {
			return PROCESS_SKIP;
		}

		IASTPointerOperator[] pointers = cppDeclarator.getPointerOperators();

		if (pointers.length == 0) {
			parent.add(NonPointerParameter.canConstify(cppDeclarator));
		} else {
			List<IDecision> decisions = PointerParameter.canConstify(cppDeclarator);
			for(IDecision decision : decisions) {
				parent.add(decision);
			}
		}

		return PROCESS_CONTINUE;
	}

}
