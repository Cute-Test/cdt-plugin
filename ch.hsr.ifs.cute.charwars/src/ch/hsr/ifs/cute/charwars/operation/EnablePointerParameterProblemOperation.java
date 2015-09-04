package ch.hsr.ifs.cute.charwars.operation;

import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;
import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;
import ch.hsr.ifs.cute.elevenator.definition.EnableCodanCheckers;
import ch.hsr.ifs.cute.elevenator.definition.IVersionModificationOperation;

public class EnablePointerParameterProblemOperation implements IVersionModificationOperation {
	@Override
	public void perform(IProject project, CPPVersion selectedVersion, boolean enabled) {
		EnableCodanCheckers.enableProblems(project, enabled, ProblemIDs.POINTER_PARAMETER_PROBLEM);
		EnableCodanCheckers.setPreference_UseWorkspaceSettings(project, false);
	}
}