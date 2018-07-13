package ch.hsr.ifs.cute.mockator.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.cute.mockator.tests.base.PluginTestSuiteBase;
import ch.hsr.ifs.cute.mockator.tests.incompleteclass.PluginTestSuiteIncompleteclass;
import ch.hsr.ifs.cute.mockator.tests.preprocessor.PluginTestSuitePreprocessor;
import ch.hsr.ifs.cute.mockator.tests.project.PluginTestSuiteProject;
import ch.hsr.ifs.cute.mockator.tests.testdouble.PluginTestSuiteTestDouble;


@RunWith(Suite.class)
@SuiteClasses({
   //@formatter:off
   PluginTestSuiteBase.class,
   PluginTestSuiteIncompleteclass.class,
   PluginTestSuiteTestDouble.class,
   PluginTestSuiteProject.class,
   PluginTestSuitePreprocessor.class,
   //@formatter:on
})
public class PluginTestSuiteAll {}
