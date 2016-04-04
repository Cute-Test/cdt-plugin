package ch.hsr.ifs.cute.templator.plugin.asttools.type.finding;

import org.eclipse.cdt.core.dom.ast.IASTName;

import ch.hsr.ifs.cute.templator.plugin.logger.TemplatorException;

public class AlreadyRelevantType extends RelevantNameType {

	public AlreadyRelevantType(IASTName definitionName) {
		super(definitionName);
	}

	@Override
	protected IASTName getTypeFromDefinition() throws TemplatorException {
		return definitionName;
	}

}
