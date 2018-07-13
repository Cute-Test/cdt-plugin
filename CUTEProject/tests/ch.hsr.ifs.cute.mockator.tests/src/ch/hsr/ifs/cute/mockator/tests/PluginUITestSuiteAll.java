package ch.hsr.ifs.cute.mockator.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.cute.mockator.tests.base.PluginUITestSuiteBase;
import ch.hsr.ifs.cute.mockator.tests.extractinterface.PluginUITestSuiteExtractInterface;
import ch.hsr.ifs.cute.mockator.tests.fakeobject.PluginUITestSuiteFakeobject;
import ch.hsr.ifs.cute.mockator.tests.linker.PluginUITestSuiteLinker;
import ch.hsr.ifs.cute.mockator.tests.mockobject.PluginUITestSuiteMockobject;
import ch.hsr.ifs.cute.mockator.tests.preprocessor.PluginUITestSuitePreprocessor;
import ch.hsr.ifs.cute.mockator.tests.testdouble.PluginUITestSuiteTestDouble;


@RunWith(Suite.class)
@SuiteClasses({
   //@formatter:off
   PluginUITestSuiteBase.class,
   PluginUITestSuiteExtractInterface.class,
   PluginUITestSuiteFakeobject.class,
   PluginUITestSuiteLinker.class,
   PluginUITestSuiteMockobject.class,
   PluginUITestSuitePreprocessor.class,
   PluginUITestSuiteTestDouble.class,
   //@formatter:on
})
public class PluginUITestSuiteAll {}
