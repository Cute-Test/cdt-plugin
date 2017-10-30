package ch.hsr.ifs.mockator.plugin.testdouble.creation.staticpoly.cppstd;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;


public class RefactoringByStdFactory {

   public MockatorRefactoring getRefactoring(CppStandard std, ICProject project, ICElement cElement, ITextSelection selection) {
      switch (std) {
      case Cpp03Std:
         return new TestDoubleCpp03Refactoring(cElement, selection, project);
      case Cpp11Std:
         return new TestDoubleCpp11Refactoring(cElement, selection, project);
      default:
         throw new MockatorException("Unexpected C++ standard");
      }
   }
}
