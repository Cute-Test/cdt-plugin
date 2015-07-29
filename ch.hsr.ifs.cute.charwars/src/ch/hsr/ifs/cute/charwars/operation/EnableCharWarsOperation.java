package ch.hsr.ifs.cute.charwars.operation;

import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;
import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;
import ch.hsr.ifs.cute.elevenator.definition.EnableCodanCheckers;
import ch.hsr.ifs.cute.elevenator.definition.IVersionModificationOperation;

public class EnableCharWarsOperation implements IVersionModificationOperation {
	@Override
	public void perform(IProject project, CPPVersion selectedVersion, boolean enabled) {
		String[] problemIDs = new String[]{
				ProblemIDs.ARRAY_PROBLEM, 
				ProblemIDs.C_STR_PROBLEM, 
				ProblemIDs.C_STRING_ALIAS_PROBLEM,
				ProblemIDs.C_STRING_CLEANUP_PROBLEM, 
				ProblemIDs.C_STRING_PARAMETER_PROBLEM,
				ProblemIDs.C_STRING_PROBLEM, 
				ProblemIDs.POINTER_PARAMETER_PROBLEM
		};
		EnableCodanCheckers.enableProblems(project, enabled, problemIDs);
		EnableCodanCheckers.setPreference_UseWorkspaceSettings(project, false);
	}
}