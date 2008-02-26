package ch.hsr.ifs.cutelauncher.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.osgi.framework.Bundle;

import ch.hsr.ifs.cutelauncher.test.hyperlinksTests.HyperlinkSuite;
import ch.hsr.ifs.cutelauncher.test.modelBuilderTests.ModelBuilderSuite;
import ch.hsr.ifs.cutelauncher.test.patternListenerTests.PatternListenerSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Cute Plugin All Tests");
		//$JUnit-BEGIN$
		suite.addTest(PatternListenerSuite.suite());
		suite.addTest(ModelBuilderSuite.suite());
		suite.addTest(SourceLookupPathTest.suite());
		
		//requires UI
		Bundle b = org.eclipse.core.runtime.Platform.getBundle("org.eclipse.ui");
		if ((b==null) || (b.getState() != Bundle.ACTIVE) || (! org.eclipse.ui.PlatformUI.isWorkbenchRunning())) {
		//headless
		}else{
			suite.addTest(CuteSuiteWizardHandlerTest.suite());
			suite.addTest(HyperlinkSuite.suite()); 
			suite.addTest(SourceActionsTest.suite());
		}
		//$JUnit-END$
		return suite;
	}

}
//adding just a single test example
//suite.addTest(new DelegateTest("testSourcelookupCustomPath"));
