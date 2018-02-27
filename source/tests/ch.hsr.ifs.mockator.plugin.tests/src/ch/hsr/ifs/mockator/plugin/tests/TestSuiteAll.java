package ch.hsr.ifs.mockator.plugin.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.mockator.plugin.tests.base.TestSuiteBase;
import ch.hsr.ifs.mockator.plugin.tests.extractinterface.TestSuiteExtractInterface;
import ch.hsr.ifs.mockator.plugin.tests.fakeobject.TestSuiteFakeobject;
import ch.hsr.ifs.mockator.plugin.tests.incompleteclass.TestSuiteIncompleteclass;
import ch.hsr.ifs.mockator.plugin.tests.linker.TestSuiteLinker;
import ch.hsr.ifs.mockator.plugin.tests.mockobject.TestSuiteMockobject;
import ch.hsr.ifs.mockator.plugin.tests.preprocessor.TestSuitePreprocessor;
import ch.hsr.ifs.mockator.plugin.tests.project.TestSuiteProject;
import ch.hsr.ifs.mockator.plugin.tests.testdouble.TestSuiteTestDouble;


@RunWith(Suite.class)
@SuiteClasses({
   //@formatter:off
   TestSuiteBase.class,
   TestSuiteExtractInterface.class,
   TestSuiteFakeobject.class,
   TestSuiteIncompleteclass.class,
   TestSuiteLinker.class,
   TestSuiteMockobject.class,
   TestSuitePreprocessor.class,
   TestSuiteProject.class,
   TestSuiteTestDouble.class,
   //@formatter:on
})
public class TestSuiteAll {}
