package ch.hsr.ifs.mockator.tests.mockobject.togglefun;

import java.util.Properties;

import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.mockator.plugin.mockobject.togglefun.ToggleTracingFunCallRefactoring;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.tests.AbstractRefactoringTest;


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
      return new ToggleTracingFunCallRefactoring(cppStandard, getActiveCElement(), selection, cproject, linkedEditStrategy);
   }
}
