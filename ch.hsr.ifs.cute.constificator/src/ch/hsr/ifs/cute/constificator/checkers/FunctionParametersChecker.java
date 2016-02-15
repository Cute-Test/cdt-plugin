package ch.hsr.ifs.cute.constificator.checkers;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;

import ch.hsr.ifs.cute.constificator.constants.Markers;
import ch.hsr.ifs.cute.constificator.visitors.FunctionParametersVisitor;

public class FunctionParametersChecker extends AbstractFunctionDefinitionChecker {

	@Override
	public ASTVisitor visitor() {
		return new FunctionParametersVisitor(this);
	}
	
	@Override
	public String definitiveID() {
		return Markers.FunctionParameters_MissingQualification;
	}

	@Override
	public String informationalID() {
		return Markers.FunctionParameters_PossiblyMissingQualification;
	}

}
