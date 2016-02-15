package ch.hsr.ifs.constificator.core.deciders.functionparameters;

import static ch.hsr.ifs.constificator.core.util.ast.Relation.*;
import static ch.hsr.ifs.constificator.core.util.type.Cast.*;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPParameter;

import ch.hsr.ifs.constificator.core.deciders.common.NonPointer;
import ch.hsr.ifs.constificator.core.deciders.decission.IDecision;
import ch.hsr.ifs.constificator.core.deciders.decission.NullDecision;

@SuppressWarnings("restriction")
public class NonPointerParameter {

	public static IDecision canConstify(ICPPASTDeclarator declarator) {
		if (declarator == null || !isDescendendOf(ICPPASTParameterDeclaration.class, declarator)) {
			return new NullDecision();
		}

		CPPParameter parameter;
		if ((parameter = as(CPPParameter.class, declarator.getName().resolveBinding())) == null) {
			return new NullDecision();
		}

		return NonPointer.decide(declarator, (ICPPASTName) declarator.getName(), parameter.getType());
	}

}
