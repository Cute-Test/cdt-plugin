package ch.hsr.ifs.cute.constificator.core.deciders.functionparameters;

import static ch.hsr.ifs.cute.constificator.core.util.ast.Relation.*;
import static ch.hsr.ifs.cute.constificator.core.util.type.Cast.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import ch.hsr.ifs.cute.constificator.core.deciders.common.Pointer;
import ch.hsr.ifs.cute.constificator.core.deciders.decision.IDecision;

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

