package ch.hsr.ifs.cute.elevenator.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;

import ch.hsr.ifs.cute.elevenator.Activator;
import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;

public class CPPVersionProjectSetting {
	public static CPPVersion loadProjectVersion(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope.getNode(Activator.PLUGIN_ID);
		if (projectNode != null) {
			String versionString = projectNode.get("c_dialect", null);
			CPPVersion version = null;
			try {
				version = CPPVersion.valueOf(versionString);
			} catch (IllegalArgumentException e) {
				Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, "Corrupted Version File");
				Activator.getDefault().getLog().log(status);
			}
			return version;
		} else {
			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, "Failed to get Project Preference Node");
			Activator.getDefault().getLog().log(status);
		}
		return null;
	}

	public static void saveProjectVersion(IProject project, CPPVersion version) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope.getNode(Activator.PLUGIN_ID);
		if (projectNode != null) {
			projectNode.put("c_dialect", version.toString());
		} else {
			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, "Failed to get Project Preference Node");
			Activator.getDefault().getLog().log(status);
		}
		try {
			projectNode.flush();
		} catch (BackingStoreException e) {
			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, "Failed to save Project CPPVersion");
			Activator.getDefault().getLog().log(status);
		}
	}
}
