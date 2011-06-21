package ch.hsr.ifs.cute.tdd;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.hsr.ifs.cute.tdd.messages"; //$NON-NLS-1$
	public static String Activator_0;
	public static String CRefactoring3_0;
	public static String ParameterHelper_1;
	public static String TddQuickFix_0;
	public static String TypeHelper_1;
	public static String TypeHelper_2;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
