package ch.hsr.ifs.cute.headers;

import org.eclipse.osgi.util.NLS;

/**
 * @since 2.0
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.hsr.ifs.cute.headers.messages"; //$NON-NLS-1$
	public static String CuteHeades_1_6_copy;
	public static String CuteHeades_1_6_copySuite;
	public static String CuteHeades_1_6_copyTestCPP;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
