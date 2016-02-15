package ch.hsr.ifs.cute.constificator.core.deciders.localvariables;

import static ch.hsr.ifs.cute.constificator.core.util.ast.Relation.*;
import static ch.hsr.ifs.cute.constificator.core.util.type.Cast.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariable;

import ch.hsr.ifs.cute.constificator.core.asttools.ASTRewriteCache;
import ch.hsr.ifs.cute.constificator.core.deciders.common.Pointer;
import ch.hsr.ifs.cute.constificator.core.deciders.decision.IDecision;

@SuppressWarnings("restriction")
public class PointerVariable {

	public static List<IDecision> canConstify(ICPPASTDeclarator declarator, ASTRewriteCache cache) {
		if (declarator == null || !isDescendendOf(IASTSimpleDeclaration.class, declarator)) {
			return new ArrayList<>();
		}

		CPPVariable pointer;
		if((pointer = as(CPPVariable.class, declarator.getName().resolveBinding())) == null) {
			return new ArrayList<>();
		}

		return Pointer.decide(declarator, (ICPPASTName) declarator.getName(), pointer.getType());
	}

}
