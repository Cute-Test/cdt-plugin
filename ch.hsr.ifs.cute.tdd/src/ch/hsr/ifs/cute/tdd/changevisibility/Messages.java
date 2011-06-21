package ch.hsr.ifs.cute.tdd.changevisibility;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.hsr.ifs.cute.tdd.changevisibility.messages"; //$NON-NLS-1$
	public static String ChangeVisibilityQuickFix_0;
	public static String ChangeVisibilityQuickFix_1;
	public static String ChangeVisibilityRefactoring_0;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
