package ch.hsr.ifs.templator.test.integrationtest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.templator.test.integrationtest.resolution.classtemplate.ClassTemplateResolutionIntegrationTestSuite;
import ch.hsr.ifs.templator.test.integrationtest.resolution.function.FunctionResolutionIntegrationTestSuite;

@RunWith(Suite.class)
@SuiteClasses({
//@formatter:off
    FunctionResolutionIntegrationTestSuite.class,
    ClassTemplateResolutionIntegrationTestSuite.class,
    //@formatter:on 
})
public class IntegrationTestSuite {
}
