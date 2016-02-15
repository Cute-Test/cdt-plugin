package ch.hsr.ifs.cute.constificator.checkers;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;

import ch.hsr.ifs.cute.constificator.constants.Markers;
import ch.hsr.ifs.cute.constificator.visitors.MemberVariablesVisitor;

public class MemberVariableChecker extends AbstractClassDefinitionChecker {

	@Override
	public ASTVisitor visitor() {
		return new MemberVariablesVisitor(this);
	}

	@Override
	public String definitiveID() {
		return Markers.ClassMembersVariables_MissingQualification;
	}

	@Override
	public String informationalID() {
		return Markers.ClassMembersVariables_PossiblyMissingQualification;
	}

}
