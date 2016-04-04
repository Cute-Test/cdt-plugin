package ch.hsr.ifs.cute.templator.plugin.asttools.data;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;

import ch.hsr.ifs.cute.templator.plugin.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.templator.plugin.asttools.templatearguments.TemplateArgumentMap;
import ch.hsr.ifs.cute.templator.plugin.logger.TemplatorException;

public abstract class AbstractTemplateInstance extends AbstractResolvedNameInfo {
	protected TemplateArgumentMap templateArgumentMap;
	protected ICPPASTTemplateParameter[] templateParameters;

	protected AbstractTemplateInstance(UnresolvedNameInfo unresolvedName, IASTDeclaration definition,
			AbstractResolvedNameInfo parent, ASTAnalyzer analyzer) throws TemplatorException {
		this(unresolvedName, definition, parent, analyzer, true);
		templateParameters = new ICPPASTTemplateParameter[0];
	}

	protected AbstractTemplateInstance(UnresolvedNameInfo unresolvedName, IASTDeclaration definition,
			AbstractResolvedNameInfo parent, ASTAnalyzer analyzer, boolean setArgumentMap) throws TemplatorException {
		super(unresolvedName, definition, parent, analyzer);

		if (setArgumentMap) {
			this.templateArgumentMap = TemplateArgumentMap.copy(getBinding().getTemplateParameterMap());
		}
	}

	protected static AbstractTemplateInstance __create(UnresolvedNameInfo unresolvedName,
			AbstractResolvedNameInfo parent, ASTAnalyzer analyzer) throws TemplatorException {
		NameTypeKind type = unresolvedName.getType();

		AbstractTemplateInstance createdInstance = null;
		if (type == NameTypeKind.FUNCTION_TEMPLATE || type == NameTypeKind.CLASS_TEMPLATE
				|| type == NameTypeKind.METHOD_TEMPLATE) {
			createdInstance = TemplateInstance.__create(unresolvedName, parent, analyzer);
		} else if (type == NameTypeKind.METHOD) {
			createdInstance = MemberFunctionInstance.__create(unresolvedName, parent, analyzer);
		} else if (type == NameTypeKind.MEMBER_ALIAS_TEMPLATE_INSTANCE
				|| type == NameTypeKind.UNKNOWN_MEMBER_ALIAS_TEMPLATE_INSTANCE) {
			createdInstance = MemberAliasTemplateInstance.__create(unresolvedName, parent, analyzer);
		}

		return createdInstance;
	}

	public ICPPASTTemplateParameter[] getTemplateParameters() {
		return templateParameters;
	}

	@Override
	public ICPPSpecialization getBinding() {
		return (ICPPSpecialization) binding;
	}

	@Override
	public TemplateArgumentMap getTemplateArgumentMap() {
		return templateArgumentMap;
	}
}
