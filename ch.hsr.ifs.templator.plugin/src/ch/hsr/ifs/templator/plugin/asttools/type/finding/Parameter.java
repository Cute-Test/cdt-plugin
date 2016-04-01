package ch.hsr.ifs.templator.plugin.asttools.type.finding;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;

import ch.hsr.ifs.templator.plugin.asttools.ASTTools;
import ch.hsr.ifs.templator.plugin.logger.TemplatorException;

public class Parameter extends RelevantNameType {

	protected Parameter(IASTName definitionName) {
		super(definitionName);
	}

	@Override
	protected IASTName getTypeFromDefinition() throws TemplatorException {
		IASTName declSpecifier = ASTTools.getParameterTypeFromDefinitionName(definitionName);
		if (declSpecifier != null) {
			IASTName resolvingName = ASTTools.extractTemplateInstanceName(declSpecifier);
			IBinding resolvedBinding = ASTTools.getUltimateBindingType(resolvingName);
			if (ASTTools.isRelevantBinding(resolvedBinding, true, false)) {
				return declSpecifier;
			}
		}
		return null;
	}

}
