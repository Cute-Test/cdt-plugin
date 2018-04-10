package ch.hsr.ifs.cute.it.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.cute.it.tests.tests.CuteExecutableProjectTest;
import ch.hsr.ifs.cute.it.tests.tests.CuteSuiteFileTest;
import ch.hsr.ifs.cute.it.tests.tests.CuteVersionTest;


@RunWith(Suite.class)
@SuiteClasses({ CuteExecutableProjectTest.class, CuteSuiteFileTest.class, CuteVersionTest.class })
public class SWTBotTestSuiteAll {

}
