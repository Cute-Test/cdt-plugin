package ch.hsr.ifs.cute.templator.plugin.asttools.data;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeferredFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethodSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethodInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownMemberClass;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownMemberClassInstance;

public enum NameTypeKind {
	FUNCTION(false),
	METHOD(false, true),
	FUNCTION_TEMPLATE(false),
	CLASS_TEMPLATE(false),
	METHOD_TEMPLATE(false, true),
	DEFERRED_FUNCTION(true),
	DEFERRED_METHOD(true, true),
	DEFERRED_CLASS_TEMPLATE(true),
	DEFERRED_MEMBER_CLASS_INSTANCE(true, true),
	UNKNOWN_MEMBER_CLASS(true, true),
	// maybe set to true and let PostResolver handle this
	TEMPLATE_PARAMETER(false, false, true),
	MEMBER_ALIAS_TEMPLATE_INSTANCE(false, true, true),
	UNKNOWN_MEMBER_ALIAS_TEMPLATE_INSTANCE(true, true, true);

	// deferred method templates result in a DEFERRED_METHOD since it is unknown whether it is a normal or a method
	// template

	private boolean member = false;
	private boolean deferred = false;
	private boolean replacementNeeded = false;

	private NameTypeKind(boolean isDeferred) {
		this(isDeferred, false, false);
	}

	private NameTypeKind(boolean isDeferred, boolean isMember) {
		this(isDeferred, isMember, false);
	}

	private NameTypeKind(boolean isDeferred, boolean member, boolean isReplacementNeeded) {
		this.deferred = isDeferred;
		this.member = member;
		this.replacementNeeded = isReplacementNeeded;
	}

	public boolean isMember() {
		return member;
	}

	public boolean isDeferred() {
		return deferred;
	}

	public void setDeferred(boolean deferred) {
		this.deferred = deferred;
	}

	public boolean isReplacementNeeded() {
		return replacementNeeded;
	}

	public void setReplacementNeeded(boolean replacementNeeded) {
		this.replacementNeeded = replacementNeeded;
	}

	public static boolean isNormalFunction(IBinding binding) {
		return binding instanceof IFunction && !(binding instanceof ICPPDeferredFunction)
				&& !(binding instanceof ICPPSpecialization) && !(binding instanceof ICPPFunctionTemplate);
	}

	public static boolean isClassTemplateInstance(IBinding binding) {
		return binding instanceof ICPPClassSpecialization;
	}

	public static boolean isFunctionTemplateInstance(IBinding binding) {
		return binding instanceof ICPPFunctionInstance;
	}

	public static boolean isMethod(IBinding binding) {
		return binding instanceof ICPPMethodSpecialization;
	}

	public static boolean isMethodTemplate(IBinding binding) {
		return binding instanceof CPPMethodInstance;
	}

	public static boolean isDeferredClassTemplate(IBinding binding) {
		return binding instanceof ICPPDeferredClassInstance;
	}

	public static boolean isDeferredFunction(IBinding binding) {
		return binding instanceof ICPPDeferredFunction;
	}

	public static boolean isDeferredMethod(IBinding binding) {
		return binding instanceof CPPUnknownMethod;
	}

	public static boolean isTemplateParameter(IBinding binding) {
		return binding instanceof ICPPTemplateParameter;
	}

	public static boolean isDeferredMemberClassInstance(IBinding binding) {
		return binding instanceof ICPPUnknownMemberClassInstance;
	}

	public static boolean isMemberAliasTemplateInstance(IBinding binding) {
		return binding instanceof ICPPAliasTemplateInstance
				&& !(((ICPPAliasTemplateInstance) binding).getType() instanceof ICPPUnknownBinding);
	}

	public static boolean isUnknownMemberAliasTemplateInstance(IBinding binding) {
		return binding instanceof ICPPAliasTemplateInstance
				&& ((ICPPAliasTemplateInstance) binding).getType() instanceof ICPPUnknownBinding;
	}

	public static boolean isUnknownMemberClass(IBinding binding) {
		return binding instanceof ICPPUnknownMemberClass;
	}

	public static NameTypeKind getType(IBinding binding) {
		NameTypeKind type = null;
		if (binding instanceof ICPPConstructor) {
			type = null;
		} else if (isDeferredClassTemplate(binding)) {
			type = NameTypeKind.DEFERRED_CLASS_TEMPLATE;
		} else if (isClassTemplateInstance(binding)) {
			type = NameTypeKind.CLASS_TEMPLATE;
		} else if (isDeferredFunction(binding)) {
			type = NameTypeKind.DEFERRED_FUNCTION;
		} else if (isDeferredMethod(binding)) {
			type = NameTypeKind.DEFERRED_METHOD;
		} else if (isMethodTemplate(binding)) {
			type = NameTypeKind.METHOD_TEMPLATE;
		} else if (isFunctionTemplateInstance(binding)) {
			type = NameTypeKind.FUNCTION_TEMPLATE;
		} else if (isNormalFunction(binding)) {
			type = NameTypeKind.FUNCTION;
		} else if (isTemplateParameter(binding)) {
			type = NameTypeKind.TEMPLATE_PARAMETER;
		} else if (isMemberAliasTemplateInstance(binding)) {
			type = NameTypeKind.MEMBER_ALIAS_TEMPLATE_INSTANCE;
		} else if (isUnknownMemberAliasTemplateInstance(binding)) {
			type = NameTypeKind.UNKNOWN_MEMBER_ALIAS_TEMPLATE_INSTANCE;
		} else if (isMethod(binding)) {
			type = NameTypeKind.METHOD;
		}
		// else if (isUnknownMemberClass(binding)) {
		// type = NameTypeKind.UNKNOWN_MEMBER_CLASS;
		// }

		return type;
	}
}