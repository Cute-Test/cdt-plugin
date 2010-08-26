package ch.hsr.ifs.core.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import ch.hsr.ifs.core.test.hyperlink.HyperlinkSuite;
import ch.hsr.ifs.core.test.modelbuilder.ModelBuilderSuite;
import ch.hsr.ifs.core.test.patternlistener.PatternListenerSuite;

public class AllCoreTests extends TestSuite{
	

	public AllCoreTests() {
		super("Cute Plugin All Core Tests"); //$NON-NLS-1$
		addTest(PatternListenerSuite.suite());
		addTest(ModelBuilderSuite.suite());
		addTest(HyperlinkSuite.suite());
	}

	public static Test suite() {
		return new AllCoreTests();
	}

}

