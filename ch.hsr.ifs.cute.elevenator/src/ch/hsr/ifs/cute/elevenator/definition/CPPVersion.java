package ch.hsr.ifs.cute.elevenator.definition;

import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.cute.elevenator.preferences.CPPVersionProjectSetting;

public enum CPPVersion {

	CPP_98("C++ 98", "c++98"), CPP_03("C++ 03", "c++03"), CPP_11("C++ 11", "c++0x"), CPP_14("C++ 14",
			"c++1y"), CPP_17("C++ 17", "c++1z");

	private String versionString;
	private String compilerVersionString;
	// java does not know enum aliases x.x
	public static final CPPVersion DEFAULT = CPP_14;

	private CPPVersion(String versionString, String compilerVersionString) {
		this.versionString = versionString;
		this.compilerVersionString = compilerVersionString;
	}

	public String getVersionString() {
		return versionString;
	}

	public String getCompilerVersionString() {
		return compilerVersionString;
	}

	public static CPPVersion getForProject(IProject project) {
		return CPPVersionProjectSetting.loadProjectVersion(project);
	}

}
