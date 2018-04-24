package ch.hsr.ifs.mockator.plugin.tests.testdouble;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.mockator.plugin.tests.testdouble.creation.staticpoly.CreateTestDoubleStaticPolyTest;
import ch.hsr.ifs.mockator.plugin.tests.testdouble.creation.staticpoly.LocalTestDoubleAlreadyProvidedTest;
import ch.hsr.ifs.mockator.plugin.tests.testdouble.creation.staticpoly.TestDoubleAlreadyProvidedInNestedNsTest;
import ch.hsr.ifs.mockator.plugin.tests.testdouble.creation.staticpoly.TestDoubleAlreadyProvidedTest;
import ch.hsr.ifs.mockator.plugin.tests.testdouble.creation.staticpoly.TestDoubleMissingTest;
import ch.hsr.ifs.mockator.plugin.tests.testdouble.creation.subtype.ArgumentToExistingClassShouldBeIgnoredTest;
import ch.hsr.ifs.mockator.plugin.tests.testdouble.creation.subtype.CtorDependencyToClassWithBaseVirtualDtorShouldBeMarkedTest;
import ch.hsr.ifs.mockator.plugin.tests.testdouble.creation.subtype.CtorDependencyToClassWithNonVirtualDtorShouldBeIgnoredTest;
import ch.hsr.ifs.mockator.plugin.tests.testdouble.creation.subtype.CtorDependencyToClassWithPrivateDtorShouldBeIgnoredTest;
import ch.hsr.ifs.mockator.plugin.tests.testdouble.creation.subtype.CtorPassByValueDependencyShouldBeIgnoredTest;
import ch.hsr.ifs.mockator.plugin.tests.testdouble.creation.subtype.CtorPtrDependencyShouldBeMarkedTest;
import ch.hsr.ifs.mockator.plugin.tests.testdouble.creation.subtype.CtorRefDependencyToBaseClassShouldBeMarkedTest;
import ch.hsr.ifs.mockator.plugin.tests.testdouble.creation.subtype.MemFunPtrDependencyInjectionShouldBeMarkedTest;
import ch.hsr.ifs.mockator.plugin.tests.testdouble.creation.subtype.MemFunRefDependencyInjectionShouldBeMarkedTest;
import ch.hsr.ifs.mockator.plugin.tests.testdouble.creation.subtype.MultipleDependenciesMissingTest;
import ch.hsr.ifs.mockator.plugin.tests.testdouble.creation.subtype.NoCtorTakingDependencyAvailableTest;
import ch.hsr.ifs.mockator.plugin.tests.testdouble.creation.subtype.PassedToFreeFunctionTest;


@RunWith(Suite.class)
//@formatter:off
@SuiteClasses({

   /*Testdouble*/
   CtorPtrDependencyShouldBeMarkedTest.class,
   PassedToFreeFunctionTest.class,
   CtorPassByValueDependencyShouldBeIgnoredTest.class,
   CtorDependencyToClassWithPrivateDtorShouldBeIgnoredTest.class,
   NoCtorTakingDependencyAvailableTest.class,
   MemFunPtrDependencyInjectionShouldBeMarkedTest.class,
   CtorRefDependencyToBaseClassShouldBeMarkedTest.class,
   CtorDependencyToClassWithNonVirtualDtorShouldBeIgnoredTest.class,
   ArgumentToExistingClassShouldBeIgnoredTest.class,
   MemFunRefDependencyInjectionShouldBeMarkedTest.class,
   CtorDependencyToClassWithBaseVirtualDtorShouldBeMarkedTest.class,
   MultipleDependenciesMissingTest.class,
   LocalTestDoubleAlreadyProvidedTest.class,
   CreateTestDoubleStaticPolyTest.class,
   TestDoubleAlreadyProvidedInNestedNsTest.class,
   TestDoubleMissingTest.class,
   TestDoubleAlreadyProvidedTest.class,

})
public class PluginTestSuiteTestDouble {
}
