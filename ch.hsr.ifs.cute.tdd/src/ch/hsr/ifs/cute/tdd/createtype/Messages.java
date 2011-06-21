package ch.hsr.ifs.cute.tdd.createtype;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.hsr.ifs.cute.tdd.createtype.messages"; //$NON-NLS-1$
	public static String CreateTypeQuickFix_0;
	public static String CreateTypeQuickFix_1;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
