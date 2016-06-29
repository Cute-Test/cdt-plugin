package ch.hsr.ifs.cute.templator.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.cute.templator.test.asttests.ASTRelatedTestSuite;
import ch.hsr.ifs.cute.templator.test.integrationtest.IntegrationTestSuite;
import ch.hsr.ifs.cute.templator.test.testhelpertest.TestHelperTestSuite;

@RunWith(Suite.class)
@SuiteClasses({
//@formatter:off
    ASTRelatedTestSuite.class,
    TestHelperTestSuite.class,
    IntegrationTestSuite.class
    //@formatter:on	
})
public class TestSuiteAll {
}
