package ch.hsr.ifs.testframework.model;

import org.eclipse.osgi.util.NLS;

/**
 * @since 3.0
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.hsr.ifs.testframework.model.messages"; //$NON-NLS-1$
	public static String ModellBuilder_0;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
