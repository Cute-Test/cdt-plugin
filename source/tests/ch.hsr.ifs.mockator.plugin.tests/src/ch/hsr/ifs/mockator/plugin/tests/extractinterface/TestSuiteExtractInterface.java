package ch.hsr.ifs.mockator.plugin.tests.extractinterface;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
//@formatter:off
@SuiteClasses({
  
   /*ExtractInterface*/
   ExtractInterfaceExternalProjectTest.class,
   ExtractInterfaceRefactoringTest.class,

})
public class TestSuiteExtractInterface {
}
