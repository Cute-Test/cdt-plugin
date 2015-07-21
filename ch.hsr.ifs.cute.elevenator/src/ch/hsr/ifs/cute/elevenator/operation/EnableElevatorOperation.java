package ch.hsr.ifs.cute.elevenator.operation;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.PreferenceConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

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

		ProjectScope ps = new ProjectScope(project);
		ScopedPreferenceStore scoped = new ScopedPreferenceStore(ps, CodanCorePlugin.PLUGIN_ID);
		scoped.setValue(PreferenceConstants.P_USE_PARENT, false);
	}
}
