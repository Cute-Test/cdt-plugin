package ch.hsr.ifs.cute.headers;

import org.eclipse.osgi.util.NLS;

/**
 * @since 2.0
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.hsr.ifs.cute.headers.messages"; //$NON-NLS-1$
	public static String CuteHeaders_1_0_copy;
	public static String CuteHeaders_1_0_copySuite;
	public static String CuteHeaders_1_0_copyTestCPP;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
