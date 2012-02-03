package ch.hsr.ifs.cute.gcov.util;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;

public final class ProjectUtil {

	private ProjectUtil() {
	}

	public static IConfiguration getConfiguration(IProject project) {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration config = info.getSelectedConfiguration();
		if (config == null) {
			config = info.getDefaultConfiguration();
		}
		return config;
	}
}
