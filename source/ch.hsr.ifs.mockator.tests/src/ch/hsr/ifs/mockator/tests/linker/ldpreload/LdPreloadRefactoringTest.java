package ch.hsr.ifs.mockator.tests.linker.ldpreload;

import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload.refactoring.LdPreloadRefactoring;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.tests.MockatorRefactoringTest;


public class LdPreloadRefactoringTest extends MockatorRefactoringTest {

   @Override
   protected Refactoring createRefactoring() {
      return new LdPreloadRefactoring(CppStandard.Cpp11Std, getActiveCElement(), selection, cproject, project);
   }
}
