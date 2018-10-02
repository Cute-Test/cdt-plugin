package ch.hsr.ifs.cute.mockator.tests.mockobject.togglefun;

import java.util.Properties;

import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.cute.mockator.tests.AbstractRefactoringTest;
import ch.hsr.ifs.cute.mockator.mockobject.togglefun.ToggleTracingFunCallRefactoring;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.project.properties.LinkedEditModeStrategy;


public class ToggleTracingFunCallRefactoringTest extends AbstractRefactoringTest {

   private CppStandard            cppStandard;
   private LinkedEditModeStrategy linkedEditStrategy;

   @Override
   protected void configureTest(final Properties p) {
      super.configureTest(p);
      cppStandard = CppStandard.fromName(p.getProperty("cppStandard"));
      linkedEditStrategy = LinkedEditModeStrategy.fromName(p.getProperty("linkedEditStrategy", "ChooseFunctions"));
      markerCount = 0;
      withCuteNature = true;
   }

   @Override
   protected Refactoring createRefactoring() {
      return new ToggleTracingFunCallRefactoring(cppStandard, getPrimaryCElementFromCurrentProject().get(), getSelectionOfPrimaryTestFile(),
            linkedEditStrategy);
   }
}
