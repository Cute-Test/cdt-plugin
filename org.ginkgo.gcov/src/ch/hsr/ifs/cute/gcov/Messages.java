package ch.hsr.ifs.cute.gcov;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.hsr.ifs.cute.gcov.messages"; //$NON-NLS-1$
	public static String DeleteMarkerJob_deleteMarker;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
