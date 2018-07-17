package ch.hsr.ifs.cute.mockator.tests.preprocessor;

import java.util.Properties;

import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.cute.mockator.tests.AbstractRefactoringTest;
import ch.hsr.ifs.cute.mockator.preprocessor.PreprocessorRefactoring;


public class PreprocessorRefactoringTest extends AbstractRefactoringTest {

   @Override
   protected void configureTest(final Properties refactoringProperties) {
      super.configureTest(refactoringProperties);
      markerCount = 0;
   }

   @Override
   protected Refactoring createRefactoring() {
      return new PreprocessorRefactoring(getPrimaryCElementFromCurrentProject().get(), getSelectionOfPrimaryTestFile(), getCurrentCProject());
   }
}
