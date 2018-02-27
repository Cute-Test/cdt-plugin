package ch.hsr.ifs.mockator.plugin.tests.base;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.mockator.plugin.tests.base.misc.DefaultCtorClassRegistryTest;
import ch.hsr.ifs.mockator.plugin.tests.base.util.ExceptionUtilTest;
import ch.hsr.ifs.mockator.plugin.tests.base.util.PathProposalUtilTest;


@RunWith(Suite.class)
//@formatter:off
@SuiteClasses({
   
   /*Base*/
   DefaultCtorClassRegistryTest.class,
   ExceptionUtilTest.class,
   PathProposalUtilTest.class,
   
})
public class TestSuiteBase {
}
