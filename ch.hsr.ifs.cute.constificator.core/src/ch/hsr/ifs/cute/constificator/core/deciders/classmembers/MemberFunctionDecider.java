package ch.hsr.ifs.cute.constificator.core.deciders.classmembers;

import static ch.hsr.ifs.cute.constificator.core.deciders.util.MemberFunctionUtil.*;
import static ch.hsr.ifs.cute.constificator.core.deciders.util.MemberVariableUtil.*;
import static ch.hsr.ifs.cute.constificator.core.util.type.Cast.*;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;

import ch.hsr.ifs.cute.constificator.core.deciders.decision.IDecision;
import ch.hsr.ifs.cute.constificator.core.deciders.decision.MemberFunctionDecision;
import ch.hsr.ifs.cute.constificator.core.deciders.decision.NullDecision;
import ch.hsr.ifs.cute.constificator.core.deciders.util.FunctionBodyVisitor;
import ch.hsr.ifs.cute.constificator.core.util.type.Pair;
import ch.hsr.ifs.cute.constificator.core.util.type.Truelean;

public class MemberFunctionDecider {

	public static IDecision canConstify(ICPPASTFunctionDeclarator declarator) {
		ICPPASTFunctionDefinition definition;
		if (declarator == null || (definition = as(ICPPASTFunctionDefinition.class, declarator.getParent())) == null) {
			return new NullDecision();
		}

		ICPPMethod method;
		if((method = as(ICPPMethod.class, declarator.getName().resolveBinding())) instanceof ICPPConstructor) {
			return new NullDecision();
		}

		ICPPField[] memberVariables = memberVariablesForOwnerOf(method);
		ICPPMethod[] memberFunctions = memberFunctionsForOwnerOf(method);

		IDecision decision = new MemberFunctionDecision(declarator);

		boolean canConstify = declarator != null && !declarator.isConst() && !method.isStatic();
		boolean mightBeConstifiable = false;

		if (canConstify) {
			canConstify = !isConstructorOrDestructor(declarator);
		}

		if (canConstify) {
			FunctionBodyVisitor visitor = new FunctionBodyVisitor(memberVariables, memberFunctions);
			definition.getBody().accept(visitor);
			canConstify = !visitor.modifiesDataMember();
		}

		if (canConstify) {
			Pair<Boolean, Boolean> overloadDescriptor = constOverloadExists(declarator);
			if (overloadDescriptor.second()) {
				canConstify = false;
				mightBeConstifiable = true;
			} else {
				canConstify = !overloadDescriptor.first();
			}
		}

		decision.decide(canConstify ? Truelean.YES : mightBeConstifiable ? Truelean.MAYBE : Truelean.NO);
		return decision;
	}

}
