package ch.hsr.ifs.cute.constificator.checkers;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;

import ch.hsr.ifs.cute.constificator.constants.Markers;
import ch.hsr.ifs.cute.constificator.visitors.LocalVariablesVisitor;

public class LocalVariablesChecker extends AbstractFunctionDefinitionChecker {

	@Override
	public ASTVisitor visitor() {
		return new LocalVariablesVisitor(this);
	}
	
	@Override
	public String definitiveID() {
		return Markers.LocalVariables_MissingQualification;
	}

	@Override
	public String informationalID() {
		return Markers.LocalVariables_PossiblyMissingQualification;
	}

}
