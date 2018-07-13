package ch.hsr.ifs.mockator.plugin.tests.fakeobject;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.mockator.plugin.tests.fakeobject.staticpoly.FakeObjectCpp03QfTest;
import ch.hsr.ifs.mockator.plugin.tests.fakeobject.staticpoly.FakeObjectCpp11QfTest;
import ch.hsr.ifs.mockator.plugin.tests.fakeobject.staticpoly.FakeObjectOperatorsQfTest;
import ch.hsr.ifs.mockator.plugin.tests.fakeobject.subtype.FakeObjectSubTypeQfTest;


@RunWith(Suite.class)
//@formatter:off
@SuiteClasses({
   /*FakeObject*/
   FakeObjectCpp03QfTest.class,
   FakeObjectCpp11QfTest.class,
   FakeObjectOperatorsQfTest.class,
   FakeObjectSubTypeQfTest.class,
   FakeObjectRefactoringTest.class,

})
public class PluginUITestSuiteFakeobject {
}
