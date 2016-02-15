package ch.hsr.ifs.cute.constificator.constants;

import org.eclipse.osgi.util.NLS;

public class Markers extends NLS {
	private static final String BUNDLE_NAME = "ch.hsr.ifs.cute.constificator.constants.markers"; //$NON-NLS-1$
	public static String LocalVariables_MissingQualification;
	public static String LocalVariables_PossiblyMissingQualification;
	public static String FunctionParameters_MissingQualification;
	public static String FunctionParameters_PossiblyMissingQualification;
	public static String ClassMembersFunctions_MissingQualification;
	public static String ClassMembersFunctions_PossiblyMissingQualification;
	public static String ClassMembersVariables_MissingQualification;
	public static String ClassMembersVariables_PossiblyMissingQualification;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Markers.class);
	}

	private Markers() {
	}
}
