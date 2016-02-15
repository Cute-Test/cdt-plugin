package ch.hsr.ifs.constificator.checkers;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;

import ch.hsr.ifs.constificator.constants.Markers;
import ch.hsr.ifs.constificator.visitors.MemberFunctionVisitor;

public class MemberFunctionChecker extends AbstractClassDefinitionChecker {

	@Override
	public ASTVisitor visitor() {
		return new MemberFunctionVisitor(this);
	}

	@Override
	public String definitiveID() {
		return Markers.ClassMembersFunctions_MissingQualification;
	}

	@Override
	public String informationalID() {
		return Markers.ClassMembersFunctions_PossiblyMissingQualification;
	}

}
