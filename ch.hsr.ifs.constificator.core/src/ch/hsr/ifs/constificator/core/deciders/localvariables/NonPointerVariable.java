package ch.hsr.ifs.constificator.core.deciders.localvariables;

import static ch.hsr.ifs.constificator.core.util.ast.Relation.*;
import static ch.hsr.ifs.constificator.core.util.type.Cast.*;

import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariable;

import ch.hsr.ifs.constificator.core.deciders.common.NonPointer;
import ch.hsr.ifs.constificator.core.deciders.decission.IDecision;
import ch.hsr.ifs.constificator.core.deciders.decission.NullDecision;

@SuppressWarnings("restriction")
public class NonPointerVariable {

	public static IDecision canConstify(ICPPASTDeclarator declarator) {
		if (declarator == null || !isDescendendOf(IASTSimpleDeclaration.class, declarator)) {
			return new NullDecision();
		}

		CPPVariable variable;
		if ((variable = as(CPPVariable.class, declarator.getName().resolveBinding())) == null) {
			return new NullDecision();
		}

		return NonPointer.decide(declarator, (ICPPASTName) declarator.getName(), variable.getType());
	}
}
