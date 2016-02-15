package ch.hsr.ifs.cute.constificator.core.deciders.util;

import static ch.hsr.ifs.cute.constificator.core.util.binding.MemberFunction.*;
import static ch.hsr.ifs.cute.constificator.core.util.trait.Types.*;
import static ch.hsr.ifs.cute.constificator.core.util.type.Cast.*;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;

import ch.hsr.ifs.cute.constificator.core.util.type.Pair;

public class MemberFunctionUtil {

	public static Pair<Boolean, Boolean> constOverloadExists(ICPPASTFunctionDeclarator member) {
		ICPPASTName name;
		if (member == null || (name = as(ICPPASTName.class, member.getName())) == null) {
			throw new NullPointerException("The supplied declarator must not be null!");
		}

		Pair<Boolean, Boolean> overloadDescription = new Pair<>(false, false);

		ICPPMethod binding;
		if ((binding = as(ICPPMethod.class, name.resolveBinding())) == null) {
			return overloadDescription;
		}

		ICPPClassType owningClass;
		if ((owningClass = as(ICPPClassType.class, binding.getOwner())) == null) {
			return overloadDescription;
		}

		ICPPMethod[] methods = owningClass.getAllDeclaredMethods();

		IType type = binding.getType();
		String idxName = binding.getName();

		for (ICPPMethod method : methods) {
			if (method.getName().equals(idxName) && areSameTypeIgnoringConst(method.getType(), type)) {
				if (isConst(method.getType(), 0)) {
					overloadDescription.first(true);
				}

				if (shadows(binding, method) || overrides(binding, method)) {
					overloadDescription.second(true);
				}
			}
		}

		return overloadDescription;
	}

	public static boolean isConstructorOrDestructor(ICPPASTFunctionDeclarator function) {
		if(function == null) {
			return false;
		}

		ICPPASTName name;
		if((name = as(ICPPASTName.class, function.getName())) == null) {
			return false;
		}

		ICPPMethod method;
		if((method = as(ICPPMethod.class, name.resolveBinding())) == null) {
			return false;
		}

		return method instanceof ICPPConstructor || method.isDestructor();
	}

	public static ICPPMethod[] memberFunctionsForOwnerOf(ICPPMethod method) {
		if(method == null) {
			return new ICPPMethod[] {};
		}

		ICPPClassType cls = method.getClassOwner();
		ICPPMethod[] methods = cls.getAllDeclaredMethods();
		return methods;
	}

}
