package ch.hsr.ifs.cute.tdd.createvariable;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.hsr.ifs.cute.tdd.createvariable.messages"; //$NON-NLS-1$
	public static String CreateLocalVariableQuickFix_0;
	public static String CreateLocalVariableQuickFix_1;
	public static String CreateMemberVariableQuickFix_0;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}