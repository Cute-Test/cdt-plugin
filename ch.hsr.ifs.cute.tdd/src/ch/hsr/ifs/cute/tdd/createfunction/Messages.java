package ch.hsr.ifs.cute.tdd.createfunction;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.hsr.ifs.cute.tdd.createfunction.messages"; //$NON-NLS-1$
	public static String LinkedModeInformation_1;
	public static String LinkedModeInformation_2;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}