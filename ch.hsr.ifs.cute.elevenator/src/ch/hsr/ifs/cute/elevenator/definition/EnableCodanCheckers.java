package ch.hsr.ifs.cute.elevenator.definition;

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemProfile;
import org.eclipse.cdt.codan.internal.core.model.CodanProblem;
import org.eclipse.core.resources.IProject;

public final class EnableCodanCheckers {
	private EnableCodanCheckers() {

	}

	public static void enableProblem(IProject project, boolean enabled, String pid) {
		IProblemProfile profile = CodanRuntime.getInstance().getCheckersRegistry()
				.getResourceProfileWorkingCopy(project);
		enableProblem(profile.getProblems(), enabled, pid);
		CodanRuntime.getInstance().getCheckersRegistry().updateProfile(project, profile); // then set for the project
	}

	private static void enableProblem(IProblem[] problems, boolean enabled, String pid) {
		for (int i = 0; i < problems.length; i++) {
			IProblem p = problems[i];
			if (p.getId().equals(pid)) {
				((CodanProblem) p).setEnabled(enabled);
				break;
			}
		}
	}

	public static void enableProblems(IProject project, boolean enabled, String... ids) {
		IProblemProfile profile = CodanRuntime.getInstance().getCheckersRegistry()
				.getResourceProfileWorkingCopy(project);
		IProblem[] problems = profile.getProblems();
		for (int i = 0; i < problems.length; i++) {
			IProblem p = problems[i];
			for (int j = 0; j < ids.length; j++) {
				String pid = ids[j];
				if (p.getId().equals(pid)) {
					((CodanProblem) p).setEnabled(enabled);
					break;
				}
			}
		}
		CodanRuntime.getInstance().getCheckersRegistry().updateProfile(project, profile); // then set for the project
	}
}
