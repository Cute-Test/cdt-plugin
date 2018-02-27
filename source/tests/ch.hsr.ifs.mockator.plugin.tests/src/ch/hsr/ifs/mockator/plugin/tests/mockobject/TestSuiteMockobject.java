package ch.hsr.ifs.mockator.plugin.tests.mockobject;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.mockator.plugin.tests.mockobject.consexp.EmptyExpectationsCpp03QfTest;
import ch.hsr.ifs.mockator.plugin.tests.mockobject.consexp.EmptyExpectationsCpp11QfTest;
import ch.hsr.ifs.mockator.plugin.tests.mockobject.convert.ConvertToMockObjectRefactoringTest;
import ch.hsr.ifs.mockator.plugin.tests.mockobject.function.LinkSuiteToRunnerRefactoringTest;
import ch.hsr.ifs.mockator.plugin.tests.mockobject.function.MockFunctionRefactoringTest;
import ch.hsr.ifs.mockator.plugin.tests.mockobject.staticpoly.MockObjectCpp03StaticPolyQfTest;
import ch.hsr.ifs.mockator.plugin.tests.mockobject.staticpoly.MockObjectCpp11StaticPolyQfTest;
import ch.hsr.ifs.mockator.plugin.tests.mockobject.staticpoly.StaticPolyExternalProjectTest;
import ch.hsr.ifs.mockator.plugin.tests.mockobject.staticpoly.StaticPolyMockObjectRefactoringTest;
import ch.hsr.ifs.mockator.plugin.tests.mockobject.subtype.MockObjectCpp03SubTypeQfTest;
import ch.hsr.ifs.mockator.plugin.tests.mockobject.subtype.MockObjectCpp11SubTypeQfTest;
import ch.hsr.ifs.mockator.plugin.tests.mockobject.subtype.SubTypeExternalProjectTest;
import ch.hsr.ifs.mockator.plugin.tests.mockobject.subtype.SubTypeMockObjectRefactoringTest;
import ch.hsr.ifs.mockator.plugin.tests.mockobject.togglefun.ToggleTracingFunCallRefactoringTest;


@RunWith(Suite.class)
//@formatter:off
@SuiteClasses({
   
   /*Mockobject*/
   EmptyExpectationsCpp03QfTest.class,
   EmptyExpectationsCpp11QfTest.class,
   
   ConvertToMockObjectRefactoringTest.class,
   
   LinkSuiteToRunnerRefactoringTest.class,
   MockFunctionRefactoringTest.class,
   
   MockObjectCpp03StaticPolyQfTest.class,
   MockObjectCpp11StaticPolyQfTest.class,
   StaticPolyExternalProjectTest.class,
   StaticPolyMockObjectRefactoringTest.class,
   
   MockObjectCpp03SubTypeQfTest.class,
   MockObjectCpp11SubTypeQfTest.class,
   SubTypeExternalProjectTest.class,
   SubTypeMockObjectRefactoringTest.class,
   
   ToggleTracingFunCallRefactoringTest.class,

})
public class TestSuiteMockobject {
}
