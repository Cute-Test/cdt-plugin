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
   PathProposalUtilTest.class,
   DefaultCtorClassRegistryTest.class,
   ExceptionUtilTest.class,
   
})
public class PluginTestSuiteBase {
}
