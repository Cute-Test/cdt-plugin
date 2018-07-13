package ch.hsr.ifs.cute.mockator.tests.testdouble;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.cute.mockator.tests.testdouble.creation.staticpoly.CreateTestDoubleCpp03QfTest;
import ch.hsr.ifs.cute.mockator.tests.testdouble.creation.staticpoly.CreateTestDoubleCpp11QfTest;
import ch.hsr.ifs.cute.mockator.tests.testdouble.creation.subtype.ByPointerCreateTestDoubleQfTest;
import ch.hsr.ifs.cute.mockator.tests.testdouble.creation.subtype.CreateTestDoubleSubTypeByPtrQfTest;
import ch.hsr.ifs.cute.mockator.tests.testdouble.creation.subtype.CreateTestDoubleSubTypeByRefQfTest;
import ch.hsr.ifs.cute.mockator.tests.testdouble.creation.subtype.IntoCtorByPointerCreateTestDoubleQfTest;
import ch.hsr.ifs.cute.mockator.tests.testdouble.creation.subtype.IntoCtorByPointerWithSutCreateTestDoubleQfTest;
import ch.hsr.ifs.cute.mockator.tests.testdouble.creation.subtype.IntoCtorByReferenceCreateTestDoubleQfTest;
import ch.hsr.ifs.cute.mockator.tests.testdouble.creation.subtype.IntoMemberFunctionByReferenceCreateTestDoubleQfTest;
import ch.hsr.ifs.cute.mockator.tests.testdouble.creation.subtype.IntoMemberFunctionOnlyFwdCreateTestDoubleQfTest;
import ch.hsr.ifs.cute.mockator.tests.testdouble.movetons.MoveToNamespaceRefactoringTest;
import ch.hsr.ifs.cute.mockator.tests.testdouble.movetons.RemoveInitMockatorRefactoringTest;


@RunWith(Suite.class)
//@formatter:off
@SuiteClasses({

   /*Testdouble*/
   IntoCtorByPointerCreateTestDoubleQfTest.class,
   IntoMemberFunctionByReferenceCreateTestDoubleQfTest.class,
   CreateTestDoubleSubTypeByRefQfTest.class,
   IntoCtorByPointerWithSutCreateTestDoubleQfTest.class,
   CreateTestDoubleSubTypeByPtrQfTest.class,
   IntoMemberFunctionOnlyFwdCreateTestDoubleQfTest.class,
   ByPointerCreateTestDoubleQfTest.class,
   IntoCtorByReferenceCreateTestDoubleQfTest.class,
   CreateTestDoubleCpp03QfTest.class,
   CreateTestDoubleCpp11QfTest.class,

   MoveToNamespaceRefactoringTest.class,
   RemoveInitMockatorRefactoringTest.class,

})
public class PluginUITestSuiteTestDouble {
}
