package ch.hsr.ifs.cute.templator.test.integrationtest.resolution.function.differentorder;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.cute.templator.test.integrationtest.resolution.function.sameorder.TwoDeducedArgumentsTest;

@RunWith(Suite.class)
@SuiteClasses({
    //@formatter:off
    TwoDeducedArgumentsTest.class,
    TemplateIdAndDeducedArgumentTest.class,
    CompleteTemplateIdWithTwoArgumentsTest.class,
    CompleteDefaultArgumentsAndEmptyTemplateIdWithArgumentsTest.class,
    //@formatter:on 
})
/**
 * Tests for function call resolutions where the function parameters have a different declaration
 * order as the template parameters in the template declaration. 
 */
public class DifferentOrderTestSuite {
}