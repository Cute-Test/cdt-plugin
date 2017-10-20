package ch.hsr.ifs.cute.swtbottest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.cute.swtbottest.tests.CuteExecutableProjectTest;
import ch.hsr.ifs.cute.swtbottest.tests.CuteSuiteFileTest;
import ch.hsr.ifs.cute.swtbottest.tests.CuteVersionTest;

@RunWith(Suite.class)
@SuiteClasses({
	CuteExecutableProjectTest.class,
	CuteSuiteFileTest.class,
	CuteVersionTest.class
})
public class AllTests {

}
