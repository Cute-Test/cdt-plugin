package ch.hsr.ifs.cute.tdd.extract;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.hsr.ifs.cute.tdd.extract.messages";
	public static String ExtractRefactoring_10;
	public static String ExtractRefactoring_11;
	public static String ExtractRefactoring_12;
	public static String ExtractRefactoring_8;
	public static String ExtractRefactoring_9;
	public static String ExtractRefactoring_NothingExtractableSelected;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
