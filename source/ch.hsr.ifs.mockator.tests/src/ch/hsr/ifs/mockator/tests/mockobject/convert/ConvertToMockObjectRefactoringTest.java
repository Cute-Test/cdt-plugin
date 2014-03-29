package ch.hsr.ifs.mockator.tests.mockobject.convert;

import java.util.Properties;

import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.mockator.plugin.mockobject.convert.ConvertToMockObjectRefactoring;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.tests.MockatorRefactoringTest;

public class ConvertToMockObjectRefactoringTest extends MockatorRefactoringTest {
  private CppStandard cppStandard;
  private LinkedEditModeStrategy linkedEditStrategy;

  @Override
  protected void configureTest(Properties refactoringProperties) {
    super.configureTest(refactoringProperties);
    cppStandard = CppStandard.fromName(refactoringProperties.getProperty("cppStandard"));
    linkedEditStrategy =
        LinkedEditModeStrategy.fromName(refactoringProperties.getProperty("linkedEditStrategy",
            "ChooseFunctions"));
    markerCount = 0;
    withCuteNature = true;
  }

  @Override
  protected Refactoring createRefactoring() {
    return new ConvertToMockObjectRefactoring(cppStandard, getActiveCElement(), selection,
        cproject, linkedEditStrategy);
  }
}
