package ch.hsr.ifs.mockator.tests.testdouble;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.mockator.tests.testdouble.creation.staticpoly.CreateTestDoubleCpp03QfTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.staticpoly.CreateTestDoubleCpp11QfTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.staticpoly.CreateTestDoubleStaticPolyTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.staticpoly.LocalTestDoubleAlreadyProvidedTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.staticpoly.TestDoubleAlreadyProvidedInNestedNsTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.staticpoly.TestDoubleAlreadyProvidedTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.staticpoly.TestDoubleMissingTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.subtype.ArgumentToExistingClassShouldBeIgnoredTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.subtype.ByPointerCreateTestDoubleQfTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.subtype.CreateTestDoubleSubTypeByPtrQfTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.subtype.CreateTestDoubleSubTypeByRefQfTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.subtype.CtorDependencyToClassWithBaseVirtualDtorShouldBeMarkedTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.subtype.CtorDependencyToClassWithNonVirtualDtorShouldBeIgnoredTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.subtype.CtorDependencyToClassWithPrivateDtorShouldBeIgnoredTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.subtype.CtorPassByValueDependencyShouldBeIgnoredTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.subtype.CtorPtrDependencyShouldBeMarkedTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.subtype.CtorRefDependencyToBaseClassShouldBeMarkedTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.subtype.IntoCtorByPointerCreateTestDoubleQfTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.subtype.IntoCtorByPointerWithSutCreateTestDoubleQfTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.subtype.IntoCtorByReferenceCreateTestDoubleQfTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.subtype.IntoMemberFunctionByReferenceCreateTestDoubleQfTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.subtype.IntoMemberFunctionOnlyFwdCreateTestDoubleQfTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.subtype.MemFunPtrDependencyInjectionShouldBeMarkedTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.subtype.MemFunRefDependencyInjectionShouldBeMarkedTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.subtype.MultipleDependenciesMissingTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.subtype.NoCtorTakingDependencyAvailableTest;
import ch.hsr.ifs.mockator.tests.testdouble.creation.subtype.PassedToFreeFunctionTest;
import ch.hsr.ifs.mockator.tests.testdouble.movetons.MoveToNamespaceRefactoringTest;
import ch.hsr.ifs.mockator.tests.testdouble.movetons.RemoveInitMockatorRefactoringTest;

@RunWith(Suite.class)
//@formatter:off
@SuiteClasses({
   
   /*Testdouble*/
   CtorPtrDependencyShouldBeMarkedTest.class,
   PassedToFreeFunctionTest.class,
   CtorPassByValueDependencyShouldBeIgnoredTest.class,
   IntoCtorByPointerCreateTestDoubleQfTest.class,
   IntoMemberFunctionByReferenceCreateTestDoubleQfTest.class,
   CtorDependencyToClassWithPrivateDtorShouldBeIgnoredTest.class,
   CreateTestDoubleSubTypeByRefQfTest.class,
   IntoCtorByPointerWithSutCreateTestDoubleQfTest.class,
   NoCtorTakingDependencyAvailableTest.class,
   CreateTestDoubleSubTypeByPtrQfTest.class,
   MemFunPtrDependencyInjectionShouldBeMarkedTest.class,
   CtorRefDependencyToBaseClassShouldBeMarkedTest.class,
   CtorDependencyToClassWithNonVirtualDtorShouldBeIgnoredTest.class,
   ArgumentToExistingClassShouldBeIgnoredTest.class,
   IntoMemberFunctionOnlyFwdCreateTestDoubleQfTest.class,
   ByPointerCreateTestDoubleQfTest.class,
   MemFunRefDependencyInjectionShouldBeMarkedTest.class,
   IntoCtorByReferenceCreateTestDoubleQfTest.class,
   CtorDependencyToClassWithBaseVirtualDtorShouldBeMarkedTest.class,
   MultipleDependenciesMissingTest.class,
   LocalTestDoubleAlreadyProvidedTest.class,
   CreateTestDoubleStaticPolyTest.class,
   TestDoubleAlreadyProvidedInNestedNsTest.class,
   TestDoubleMissingTest.class,
   TestDoubleAlreadyProvidedTest.class,
   CreateTestDoubleCpp03QfTest.class,
   CreateTestDoubleCpp11QfTest.class,

   MoveToNamespaceRefactoringTest.class,
   RemoveInitMockatorRefactoringTest.class,

})
public class TestSuiteTestDouble {
}
