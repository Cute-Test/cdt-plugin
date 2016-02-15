package ch.hsr.ifs.cute.constificator.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.cute.constificator.test.checker.CheckerTests;
import ch.hsr.ifs.cute.constificator.test.quickfix.QuickFixTests;

@RunWith(Suite.class)
@SuiteClasses({
//@formatter:off
	CheckerTests.class,
	QuickFixTests.class
//@formatter:on
})
public class ConstificatorTests {

}
