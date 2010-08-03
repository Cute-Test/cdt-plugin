package ch.hsr.ifs.cute.headers;

import org.eclipse.osgi.util.NLS;

/**
 * @since 1.2
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.hsr.ifs.cute.headers.messages"; //$NON-NLS-1$
	public static String CuteHeades_1_5_copy;
	public static String CuteHeades_1_5_copySuite;
	public static String CuteHeades_1_5_copyTestCPP;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
