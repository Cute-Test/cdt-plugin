package ch.hsr.ifs.cute.mockator.tests.mockobject.subtype;

import java.util.Properties;

import ch.hsr.ifs.cute.mockator.mockobject.qf.MockObjectRefactoring;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.cute.mockator.tests.AbstractRefactoringTest;


public class SubTypeMockObjectRefactoringTest extends AbstractRefactoringTest {

   private CppStandard            cppStandard;
   private LinkedEditModeStrategy linkedEditStrategy;

   @Override
   protected void configureTest(final Properties p) {
      super.configureTest(p);
      cppStandard = CppStandard.fromName(p.getProperty("cppStandard"));
      linkedEditStrategy = LinkedEditModeStrategy.fromName(p.getProperty("linkedEditStrategy", "ChooseFunctions"));
      withCuteNature = true;
   }

   @Override
   protected MockatorRefactoring createRefactoring() {
      return new MockObjectRefactoring(cppStandard, getPrimaryCElementFromCurrentProject().get(), getSelectionOfPrimaryTestFile(),
            getCurrentCProject(), linkedEditStrategy);
   }
}
