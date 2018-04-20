package ch.hsr.ifs.mockator.plugin.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.mockator.plugin.tests.base.PluginTestSuiteBase;
import ch.hsr.ifs.mockator.plugin.tests.incompleteclass.PluginTestSuiteIncompleteclass;
import ch.hsr.ifs.mockator.plugin.tests.preprocessor.PluginTestSuitePreprocessor;
import ch.hsr.ifs.mockator.plugin.tests.project.PluginTestSuiteProject;
import ch.hsr.ifs.mockator.plugin.tests.testdouble.PluginTestSuiteTestDouble;


@RunWith(Suite.class)
@SuiteClasses({
   //@formatter:off
   PluginTestSuiteBase.class,
//   TODO(Tobias Stauber) reenable after testing
   PluginTestSuiteIncompleteclass.class,
   PluginTestSuiteTestDouble.class,
   PluginTestSuiteProject.class,
   PluginTestSuitePreprocessor.class,
   //@formatter:on
})
public class PluginTestSuiteAll {}
