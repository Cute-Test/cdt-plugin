package ch.hsr.ifs.cute.boost;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.hsr.ifs.cute.boost.messages"; //$NON-NLS-1$
	public static String BoostHandler_beginTaskFolders;
	public static String BoostHandler_copy;
	public static String BoostWizardAddition_0;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
