package ch.hsr.ifs.mockator.tests.base;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.mockator.tests.base.misc.CastHelperTest;
import ch.hsr.ifs.mockator.tests.base.misc.DefaultCtorClassRegistryTest;
import ch.hsr.ifs.mockator.tests.base.util.ExceptionUtilTest;
import ch.hsr.ifs.mockator.tests.base.util.PathProposalUtilTest;
import ch.hsr.ifs.mockator.tests.base.util.StringUtilTest;

@RunWith(Suite.class)
//@formatter:off
@SuiteClasses({
   
   /*Base*/
   CastHelperTest.class,
   DefaultCtorClassRegistryTest.class,
   ExceptionUtilTest.class,
   PathProposalUtilTest.class,
   StringUtilTest.class,
   ExceptionTest.class,
   
})
public class TestSuiteBase {
}
