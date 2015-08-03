package ch.hsr.ifs.cute.charwars.operation;

import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;
import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;
import ch.hsr.ifs.cute.elevenator.definition.EnableCodanCheckers;
import ch.hsr.ifs.cute.elevenator.definition.IVersionModificationOperation;

public class EnableCStringCleanupProblemOperation implements IVersionModificationOperation {
	@Override
	public void perform(IProject project, CPPVersion selectedVersion, boolean enabled) {
		EnableCodanCheckers.enableProblems(project, enabled, ProblemIDs.C_STRING_CLEANUP_PROBLEM);
		EnableCodanCheckers.setPreference_UseWorkspaceSettings(project, false);
	}
}