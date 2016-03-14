package ch.hsr.ifs.cute.constificator.core.util.trait;

import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

@SuppressWarnings("restriction")
public class Types {

	public static boolean areSameTypeIgnoringConst(IType suspect, IType original) {
		while (original != null && suspect != null) {
			if (original.getClass().equals(suspect.getClass())) {
				if (original instanceof ITypeContainer) {
					original = ((ITypeContainer) original).getType();
					suspect = ((ITypeContainer) suspect).getType();
				} else if (original instanceof ICPPFunctionType) {

					IType[] parameterTypesOriginal = ((ICPPFunctionType) original).getParameterTypes();
					IType[] parameterTypesCalled = ((IFunctionType) suspect).getParameterTypes();
					if (parameterTypesOriginal.length == parameterTypesCalled.length) {
						boolean same = true;
						for (int i = 0; i < parameterTypesOriginal.length; i++) {
							same &= areSameTypeIgnoringConst(parameterTypesOriginal[i], parameterTypesCalled[i]);
						}

						if(((ICPPFunctionType) original).hasRefQualifier() || ((ICPPFunctionType) original).hasRefQualifier()) {
							same &= ((ICPPFunctionType) original).isRValueReference() == ((ICPPFunctionType) suspect).isRValueReference();
						}

						return same;

					} else {
						return false;
					}
				} else {
					return original.isSameType(suspect);
				}
			} else if (original instanceof ITypeContainer) {
				original = ((ITypeContainer) original).getType();
			} else if (suspect instanceof ITypeContainer) {
				suspect = ((ITypeContainer) suspect).getType();
			} else {
				return false;
			}
		}
		return false;
	}

	public static boolean isConst(IType suspect, int pointerLevel) {
		
		suspect = SemanticUtil.getSimplifiedType(suspect);
		
		for (; pointerLevel > 0; --pointerLevel) {
			if (suspect instanceof ITypeContainer) {
				suspect = SemanticUtil.getSimplifiedType(((ITypeContainer) suspect).getType());
			} else {
				return false;
			}
		}

		if (suspect instanceof IQualifierType) {
			return ((IQualifierType) suspect).isConst();
		} else if (suspect instanceof IPointerType) {
			return ((IPointerType) suspect).isConst();
		} else if (suspect instanceof ICPPFunctionType) {
			return ((ICPPFunctionType) suspect).isConst();
		}

		return false;
	}

	public static boolean isMoreConst(IType suspect, IType original) {
		suspect = decay(suspect);
		original = decay(original);

		boolean originalConst = isConst(original, 0);

		boolean suspectConst = isConst(suspect, 0);

		while (original instanceof ITypeContainer && suspect instanceof ITypeContainer) {
			if (originalConst != suspectConst) {
				return !originalConst && suspectConst;
			}

			original = decay(original);
			suspect = decay(suspect);

			originalConst = isConst(original, 0);

			suspectConst = isConst(suspect, 0);
		}

		return isConst(suspect, 0) && !isConst(original, 0);
	}

	public static boolean isReference(IType suspect) {
		return SemanticUtil.getSimplifiedType(suspect) instanceof ICPPReferenceType;
	}

	public static boolean isArray(IType suspect) {
		return SemanticUtil.getSimplifiedType(suspect) instanceof IArrayType;
	}

	public static boolean referencesNonPointer(IType suspect) {
		if (isReference(suspect)) {
			suspect = decay(suspect);
			return !(suspect instanceof IPointerType);
		}

		return false;
	}

	public static int pointerLevels(IType type) {
		int levels = 0;
		type = SemanticUtil.getSimplifiedType(type);

		while (!(type instanceof IBasicType) && !(type instanceof ICPPClassType) && !(type instanceof IProblemType) && !(type instanceof ICPPEnumeration) && !(type instanceof ICPPFunctionType) && !(type instanceof IArrayType) && !(type instanceof ICPPTemplateTypeParameter)) {
			if (!isReference(type) && !(type instanceof IQualifierType)) {
				++levels;
			}
			type = decay(type);
		}

		return levels;
	}

	private static IType decay(IType type) {
		if (type instanceof ITypeContainer) {
			type = ((ITypeContainer) type).getType();
		}

		return SemanticUtil.getSimplifiedType(type);
	}

}
