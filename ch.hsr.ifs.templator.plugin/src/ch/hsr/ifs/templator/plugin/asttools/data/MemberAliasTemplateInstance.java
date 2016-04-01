package ch.hsr.ifs.templator.plugin.asttools.data;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

import ch.hsr.ifs.templator.plugin.asttools.ASTAnalyzer;
import ch.hsr.ifs.templator.plugin.asttools.ASTTools;
import ch.hsr.ifs.templator.plugin.asttools.resolving.ClassTemplateResolver;
import ch.hsr.ifs.templator.plugin.asttools.resolving.PostResolver;
import ch.hsr.ifs.templator.plugin.asttools.templatearguments.TemplateArgumentMap;
import ch.hsr.ifs.templator.plugin.logger.TemplatorException;

public class MemberAliasTemplateInstance extends TemplateInstance {
	protected IASTName aliasedTypeName;
	protected ICPPAliasTemplate aliasTemplate;
	protected ICPPAliasTemplateInstance originalBinding;

	protected MemberAliasTemplateInstance(UnresolvedNameInfo unresolvedName, ICPPAliasTemplateInstance aliasInstance,
			ICPPASTTemplateDeclaration definition, ICPPAliasTemplate aliasTemplate, IASTName aliasedTypeName,
			AbstractResolvedNameInfo parent, ASTAnalyzer analyzer) throws TemplatorException {
		super(unresolvedName, definition, parent, analyzer, false);

		this.aliasedTypeName = aliasedTypeName;
		this.aliasTemplate = aliasTemplate;
		this.originalBinding = aliasInstance;
		this.templateArgumentMap = new TemplateArgumentMap(parent.getTemplateArgumentMap());
		addAliasArguments();
	}

	public static MemberAliasTemplateInstance __create(UnresolvedNameInfo unresolvedName,
			AbstractResolvedNameInfo parent, ASTAnalyzer analyzer) throws TemplatorException {
		ICPPAliasTemplateInstance aliasInstance = (ICPPAliasTemplateInstance) unresolvedName.getBinding();

		ICPPAliasTemplate aliasTemplate = aliasInstance.getTemplateDefinition();
		ICPPASTTemplateDeclaration aliasDeclaration = analyzer.getTemplateDeclaration(aliasTemplate);

		IASTName aliasDefinitionName = analyzer.getDefinition(aliasTemplate);
		IASTName aliasedTypeName = ASTTools.getUsingTypeFromDefinitionName(aliasDefinitionName);
		return new MemberAliasTemplateInstance(unresolvedName, aliasInstance, aliasDeclaration, aliasTemplate,
				aliasedTypeName, parent, analyzer);
	}

	public IASTName getAliasedTypeName() {
		return aliasedTypeName;
	}

	public ICPPAliasTemplate getAliasTemplate() {
		return aliasTemplate;
	}

	private void addAliasArguments() throws TemplatorException {
		ICPPASTTemplateId aliasTemplateId = PostResolver.getTemplateId(resolvingName, parent, parent.getAnalyzer());
		ICPPTemplateArgument[] args = null;
		try {
			args = CPPTemplates.createTemplateArgumentArray(aliasTemplateId);
			args = ClassTemplateResolver.addDefaultArguments(aliasTemplate, args, aliasTemplateId);
		} catch (Exception e) {
			throw new TemplatorException(e);
		}
		if (args == null) {
			throw new TemplatorException(
					"Could not determine the template arguments for " + aliasTemplate.getName() + ".");
		}

		ICPPTemplateParameter[] aliasTemplateParameters = aliasTemplate.getTemplateParameters();
		for (ICPPTemplateParameter param : aliasTemplateParameters) {
			templateArgumentMap.put(param, getArgument(param));
		}
	}
}
