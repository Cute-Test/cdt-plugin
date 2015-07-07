package ch.hsr.ifs.cute.elevenator;

public enum CppVersions {

	CPP_98("C++ 98", "c++98"), CPP_03("C++ 03", "c++03"), CPP_11("C++ 11", "c++11"), CPP_14("C++ 14",
			"c++14"), CPP_17("C++ 17", "c++17");

	private String versionString;
	private String compilerVersionString;
	// java does not know enum aliases x.x
	public static final CppVersions DEFAULT = CPP_14;

	private CppVersions(String versionString, String compilerVersionString) {
		this.versionString = versionString;
		this.compilerVersionString = compilerVersionString;
	}

	public String getVersionString() {
		return versionString;
	}

	public String getCompilerVersionString() {
		return compilerVersionString;
	}

}
