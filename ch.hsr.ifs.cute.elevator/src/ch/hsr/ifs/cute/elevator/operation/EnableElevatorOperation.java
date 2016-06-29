package ch.hsr.ifs.cute.elevator.operation;

import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.cute.elevator.checker.InitializationChecker;
import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;
import ch.hsr.ifs.cute.elevenator.definition.EnableCodanCheckers;
import ch.hsr.ifs.cute.elevenator.definition.IVersionModificationOperation;

public class EnableElevatorOperation implements IVersionModificationOperation {

	@Override
	public void perform(IProject project, CPPVersion selectedVersion, boolean enabled) {

		// EnableCodanCheckers.enableProblem(project, true, InitializationChecker.UNINITIALIZED_VAR);
		// EnableCodanCheckers.enableProblem(project, false, InitializationChecker.DEFAULT_CTOR);
		// EnableCodanCheckers.enableProblem(project, true, InitializationChecker.NULL_MACRO);

		EnableCodanCheckers.enableProblems(project, enabled, InitializationChecker.UNINITIALIZED_VAR,
				InitializationChecker.DEFAULT_CTOR, InitializationChecker.NULL_MACRO);

		EnableCodanCheckers.setPreference_UseWorkspaceSettings(project, false);

	}

}
