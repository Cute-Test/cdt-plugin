package ch.hsr.ifs.cute.mockator.it.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({
//@formatter:off
	MockatorInCUTEWizardTest.class,
//	ToggleMockatorProjectSupportTest.class,
	CProjectsNotSupportedTest.class
//@formatter:on
})
public class SWTBotTestSuiteAll {}
