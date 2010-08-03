package ch.hsr.ifs.cute.gcov.parser;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.hsr.ifs.cute.gcov.parser.messages"; //$NON-NLS-1$
	public static String LaunchObserver_parse;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
