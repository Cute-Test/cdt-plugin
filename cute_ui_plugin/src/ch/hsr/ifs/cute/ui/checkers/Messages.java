package ch.hsr.ifs.cute.ui.checkers;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.hsr.ifs.cute.ui.checkers.messages"; //$NON-NLS-1$
	public static String UnregisteredTestResolution_0;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
