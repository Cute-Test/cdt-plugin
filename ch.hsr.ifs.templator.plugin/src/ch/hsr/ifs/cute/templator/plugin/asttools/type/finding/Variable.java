package ch.hsr.ifs.cute.templator.plugin.asttools.type.finding;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;

import ch.hsr.ifs.cute.templator.plugin.asttools.ASTTools;
import ch.hsr.ifs.cute.templator.plugin.logger.TemplatorException;

public class Variable extends RelevantNameType {

	protected Variable(IASTName definitionName) {
		super(definitionName);
	}

	@Override
	protected IASTName getTypeFromDefinition() throws TemplatorException {
		IASTSimpleDeclaration declaration = ASTTools.findFirstAncestorByType(definitionName,
				IASTSimpleDeclaration.class, 3);
		return ASTTools.getName(declaration);
	}

}