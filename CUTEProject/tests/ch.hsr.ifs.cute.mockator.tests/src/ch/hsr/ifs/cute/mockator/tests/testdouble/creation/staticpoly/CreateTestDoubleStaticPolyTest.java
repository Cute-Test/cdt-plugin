package ch.hsr.ifs.cute.mockator.tests.testdouble.creation.staticpoly;

import java.util.Properties;

import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.testdouble.creation.staticpoly.cppstd.RefactoringByStdFactory;
import ch.hsr.ifs.cute.mockator.tests.AbstractRefactoringTest;


public class CreateTestDoubleStaticPolyTest extends AbstractRefactoringTest {

   private CppStandard cppStandard;

   @Override
   protected void configureTest(final Properties refactoringProperties) {
      super.configureTest(refactoringProperties);
      cppStandard = CppStandard.fromName(refactoringProperties.getProperty("cppStandard"));
   }

   @Override
   protected Refactoring createRefactoring() {
      return new RefactoringByStdFactory().getRefactoring(getPrimaryCElementFromCurrentProject().get(), getSelectionOfPrimaryTestFile(),
            getCurrentCProject(), cppStandard);
   }
}
