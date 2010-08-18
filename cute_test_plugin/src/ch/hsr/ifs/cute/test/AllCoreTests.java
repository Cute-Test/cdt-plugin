package ch.hsr.ifs.cute.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import ch.hsr.ifs.cute.test.modelBuilderTests.ModelBuilderSuite;
import ch.hsr.ifs.cute.test.patternListenerTests.PatternListenerSuite;

public class AllCoreTests extends TestSuite{
	

	public AllCoreTests() {
		super("Cute Plugin All Core Tests"); //$NON-NLS-1$
		addTest(PatternListenerSuite.suite());
		addTest(ModelBuilderSuite.suite());
//		addTest(SourceLookupPathTest.suite());
	}

	public static Test suite() {
		return new AllCoreTests();
	}

}

