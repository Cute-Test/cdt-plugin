package ch.hsr.ifs.templator.test.integrationtest.resolution.classtemplate;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
//@formatter:off
    ExplicitSpecializationSelection.class,
    PartialSpecializationSelection.class,
    SimpleInstantiation.class
    //@formatter:on 
})
public class ClassTemplateResolutionIntegrationTestSuite {
}
