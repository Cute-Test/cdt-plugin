package ch.hsr.ifs.mockator.tests.testdouble.creation.staticpoly;

import java.util.Properties;

import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.staticpoly.cppstd.RefactoringByStdFactory;
import ch.hsr.ifs.mockator.tests.MockatorRefactoringTest;


public class CreateTestDoubleStaticPolyTest extends MockatorRefactoringTest {

   private CppStandard cppStandard;

   @Override
   protected void configureTest(final Properties refactoringProperties) {
      super.configureTest(refactoringProperties);
      cppStandard = CppStandard.fromName(refactoringProperties.getProperty("cppStandard"));
   }

   @Override
   protected Refactoring createRefactoring() {
      return new RefactoringByStdFactory().getRefactoring(cppStandard, cproject, getActiveCElement(), selection);
   }
}
