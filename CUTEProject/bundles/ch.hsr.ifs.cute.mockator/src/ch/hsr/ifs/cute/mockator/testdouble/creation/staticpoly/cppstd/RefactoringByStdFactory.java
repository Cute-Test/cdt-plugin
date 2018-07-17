package ch.hsr.ifs.cute.mockator.testdouble.creation.staticpoly.cppstd;

import java.util.Optional;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;

import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorRefactoring;


public class RefactoringByStdFactory {

   public MockatorRefactoring getRefactoring(final ICElement cElement, final Optional<ITextSelection> selection, final ICProject project,
         final CppStandard std) {
      switch (std) {
      case Cpp03Std:
         return new TestDoubleCpp03Refactoring(cElement, selection, project);
      case Cpp11Std:
         return new TestDoubleCpp11Refactoring(cElement, selection, project);
      default:
         throw new ILTISException("Unexpected C++ standard").rethrowUnchecked();
      }
   }
}
