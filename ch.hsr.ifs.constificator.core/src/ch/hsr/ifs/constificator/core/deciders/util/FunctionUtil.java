package ch.hsr.ifs.constificator.core.deciders.util;

import static ch.hsr.ifs.constificator.core.util.ast.DOM.*;
import static ch.hsr.ifs.constificator.core.util.trait.Types.*;
import static ch.hsr.ifs.constificator.core.util.type.Cast.*;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethodSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;

@SuppressWarnings("restriction")
public class FunctionUtil {

	public static boolean hasConstOverload(ICPPASTName name, int parameterIndex, int pointerLevel) {
		Set<ICPPASTFunctionDeclarator> declarations = declarationsFor(name, true);

		if (name == null) {
			return false;
		}

		ICPPFunction called;
		if ((called = as(ICPPFunction.class, name.resolveBinding())) == null) {
			return false;
		}

		if (called.getParameters().length <= parameterIndex) {
			return false;
		}

		ICPPParameter calledParameter = called.getParameters()[parameterIndex];
		for (ICPPASTFunctionDeclarator decl : declarations) {
			ICPPParameter currentParameter = as(ICPPParameter.class,
					decl.getParameters()[parameterIndex].getDeclarator().getName().resolveBinding());

			if (isMoreConst(currentParameter.getType(), calledParameter.getType())) {
				return true;
			}
		}

		return false;
	}

	public static Set<ICPPASTFunctionDeclarator> declarationsFor(ICPPASTName name, boolean includeOverloads) {
		Set<ICPPASTFunctionDeclarator> decls = new HashSet<>();

		if (name == null) {
			return decls;
		}

		ICPPFunction called;
		if ((called = as(ICPPFunction.class, name.resolveBinding())) == null) {
			return decls;
		}

		IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(name, false, null);

		for (IBinding current : bindings) {
			if(current instanceof ICPPFunction) {
				if (matchTypes(called, (ICPPFunction) current, includeOverloads)) {
					IIndex index = name.getTranslationUnit().getIndex();
					ICProject project = name.getTranslationUnit().getOriginatingTranslationUnit().getCProject();
					decls.addAll(resolveBindingToNodeSet(ICPPASTFunctionDeclarator.class, current, index, project));
				}
			}
		}

		return decls;
	}

	private static boolean matchTypes(ICPPFunction original, ICPPFunction suspect, boolean ignoreConst) {
		ICPPFunctionType originalType = original.getType();
		ICPPFunctionType suspectType = suspect.getType();

		if (!originalType.getReturnType().isSameType(suspectType.getReturnType())) {
			return false;
		}

		IType[] originalParameterTypes = originalType.getParameterTypes();
		IType[] suspectParameterTypes = suspectType.getParameterTypes();

		if (originalParameterTypes.length != suspectParameterTypes.length) {
			return false;
		}

		for (int index = 0; index < originalParameterTypes.length; ++index) {
			if (ignoreConst) {
				if (!areSameTypeIgnoringConst(suspectParameterTypes[index], originalParameterTypes[index])) {
					return false;
				}
			} else {
				if (!suspectParameterTypes[index].isSameType(originalParameterTypes[index])) {
					return false;
				}
			}
		}

		return true;
	}

	private static ICPPASTFunctionDeclarator getDefinition(ICPPFunction function) {
		Class<?> currentClass = function.getClass();

		if(function instanceof ICPPMethodSpecialization) {
			return null;
		}

		while (currentClass != null && currentClass.getSuperclass() != null) {
			try {
				Field field = currentClass.getDeclaredField("definition");
				field.setAccessible(true);
				Object value = field.get(function);
				field.setAccessible(false);

				if (value instanceof ICPPASTFunctionDeclarator) {
					return (ICPPASTFunctionDeclarator) value;
				} else {
					return null;
				}
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				currentClass = currentClass.getSuperclass();
			}
		}

		return null;
	}

}