package ch.hsr.ifs.mockator.plugin.tests.linker;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.mockator.plugin.tests.linker.gnuoption.DeleteWrappedFunctionQfTest;
import ch.hsr.ifs.mockator.plugin.tests.linker.gnuoption.GnuOptionRefactoringTest;
import ch.hsr.ifs.mockator.plugin.tests.linker.ldpreload.LdPreloadRefactoringTest;
import ch.hsr.ifs.mockator.plugin.tests.linker.shadowfun.ShadowFunctionRefProjectTest;
import ch.hsr.ifs.mockator.plugin.tests.linker.shadowfun.ShadowFunctionRefactoringTest;


@RunWith(Suite.class)
//@formatter:off
@SuiteClasses({

   /*Linker*/
   DeleteWrappedFunctionQfTest.class,
   GnuOptionRefactoringTest.class,
   LdPreloadRefactoringTest.class,
   ShadowFunctionRefactoringTest.class,
   ShadowFunctionRefProjectTest.class,

})
public class TestSuiteLinker {
}
