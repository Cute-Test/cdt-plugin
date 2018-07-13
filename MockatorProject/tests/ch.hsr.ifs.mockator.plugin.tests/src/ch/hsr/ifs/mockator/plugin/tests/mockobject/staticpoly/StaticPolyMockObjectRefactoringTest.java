package ch.hsr.ifs.mockator.plugin.tests.mockobject.staticpoly;

import java.util.Properties;

import ch.hsr.ifs.mockator.plugin.mockobject.qf.MockObjectRefactoring;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.mockator.plugin.tests.AbstractRefactoringTest;


public class StaticPolyMockObjectRefactoringTest extends AbstractRefactoringTest {

   private CppStandard            cppStandard;
   private LinkedEditModeStrategy linkedEditStrategy;

   @Override
   protected void configureTest(final Properties p) {
      super.configureTest(p);
      cppStandard = CppStandard.fromName(p.getProperty("cppStandard"));
      linkedEditStrategy = LinkedEditModeStrategy.fromName(p.getProperty("linkedEditStrategy", "ChooseFunctions"));
   }

   @Override
   protected MockatorRefactoring createRefactoring() {
      return new MockObjectRefactoring(cppStandard, getPrimaryCElementFromCurrentProject().get(), getSelectionOfPrimaryTestFile(),
            getCurrentCProject(), linkedEditStrategy);
   }
}
