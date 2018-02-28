package ch.hsr.ifs.mockator.plugin.tests.preprocessor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
//@formatter:off
@SuiteClasses({
  
   /*Preprocessor*/
   PreprocessorRefactoringTest.class,
   TraceFunctionCheckerTest.class,

})
public class TestSuitePreprocessor {
}
