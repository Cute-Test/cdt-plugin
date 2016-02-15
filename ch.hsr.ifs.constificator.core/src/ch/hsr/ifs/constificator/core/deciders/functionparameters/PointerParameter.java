package ch.hsr.ifs.constificator.core.deciders.functionparameters;

import static ch.hsr.ifs.constificator.core.util.ast.Relation.*;
import static ch.hsr.ifs.constificator.core.util.type.Cast.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;

import ch.hsr.ifs.constificator.core.deciders.common.Pointer;
import ch.hsr.ifs.constificator.core.deciders.decission.IDecision;

@SuppressWarnings("restriction")
public class PointerParameter {

	public static List<IDecision> canConstify(ICPPASTDeclarator declarator) {
		if (declarator == null || !isDescendendOf(ICPPASTParameterDeclaration.class, declarator)) {
			return new ArrayList<>();
		}

		ICPPParameter parameter;
		if((parameter = as(ICPPParameter.class, declarator.getName().resolveBinding())) == null) {
			return new ArrayList<>();
		}

		return Pointer.decide(declarator, (ICPPASTName) declarator.getName(), parameter.getType());
	}

}

