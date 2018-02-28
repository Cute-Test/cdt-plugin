package ch.hsr.ifs.mockator.plugin.tests.preprocessor;

import java.util.Properties;

import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.mockator.plugin.preprocessor.PreprocessorRefactoring;
import ch.hsr.ifs.mockator.plugin.tests.AbstractRefactoringTest;


public class PreprocessorRefactoringTest extends AbstractRefactoringTest {

   @Override
   protected void configureTest(final Properties refactoringProperties) {
      super.configureTest(refactoringProperties);
      markerCount = 0;
   }

   @Override
   protected Refactoring createRefactoring() {
      return new PreprocessorRefactoring(getActiveCElement(), selection, currentCproject);
   }
}
