package ch.hsr.ifs.templator.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.templator.test.asttests.ASTRelatedTestSuite;
import ch.hsr.ifs.templator.test.integrationtest.IntegrationTestSuite;
import ch.hsr.ifs.templator.test.testhelpertest.TestHelperTestSuite;

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
