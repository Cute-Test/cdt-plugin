package ch.hsr.ifs.mockator.plugin.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.mockator.plugin.tests.extractinterface.PluginUITestSuiteExtractInterface;
import ch.hsr.ifs.mockator.plugin.tests.fakeobject.PluginUITestSuiteFakeobject;
import ch.hsr.ifs.mockator.plugin.tests.linker.PluginUITestSuiteLinker;
import ch.hsr.ifs.mockator.plugin.tests.mockobject.PluginUITestSuiteMockobject;
import ch.hsr.ifs.mockator.plugin.tests.preprocessor.PluginUITestSuitePreprocessor;
import ch.hsr.ifs.mockator.plugin.tests.testdouble.PluginUITestSuiteTestDouble;


@RunWith(Suite.class)
@SuiteClasses({
   //@formatter:off
   PluginUITestSuiteExtractInterface.class,
   PluginUITestSuiteFakeobject.class,
   PluginUITestSuiteLinker.class,
   PluginUITestSuiteMockobject.class,
   PluginUITestSuitePreprocessor.class,
   PluginUITestSuiteTestDouble.class,
   //@formatter:on
})
public class PluginUITestSuiteAll {}
