package ch.hsr.ifs.cute.mockator.testdouble.qf;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;

import ch.hsr.ifs.cute.mockator.incompleteclass.MissingMemFunFinder;
import ch.hsr.ifs.cute.mockator.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.cute.mockator.incompleteclass.staticpoly.StaticPolyMissingMemFunFinder;
import ch.hsr.ifs.cute.mockator.incompleteclass.subtype.SubtypeMissingMemFunFinder;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.cute.mockator.refsupport.utils.ClassPublicVisibilityInserter;
import ch.hsr.ifs.cute.mockator.testdouble.entities.TestDouble;


public abstract class AbstractTestDoubleRefactoring extends MockatorRefactoring {

   protected CppStandard cppStd;
   protected TestDouble  testDouble;

   public AbstractTestDoubleRefactoring(final CppStandard cppStd, final ICElement cElement, final Optional<ITextSelection> selection,
                                        final ICProject project) {
      super(cElement, selection, project);
      this.cppStd = cppStd;
   }

   @Override
   public RefactoringStatus checkInitialConditions(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
      final RefactoringStatus status = super.checkInitialConditions(pm);
      final Optional<ICPPASTCompositeTypeSpecifier> classInSelection = findFirstEnclosingClass(selection);

      if (!classInSelection.isPresent()) {
         status.addFatalError("Could not find a class in the current selection");
      } else {
         testDouble = createTestDouble(classInSelection.get());
      }

      return status;
   }

   protected ClassPublicVisibilityInserter getPublicVisibilityInserter(final ASTRewrite rewriter) {
      return new ClassPublicVisibilityInserter(testDouble.getKlass(), rewriter);
   }

   protected abstract TestDouble createTestDouble(ICPPASTCompositeTypeSpecifier selectedClass);

   protected Collection<? extends MissingMemberFunction> collectMissingMemFuns(final IProgressMonitor pm) {
      return testDouble.collectMissingMemFuns(getMissingMemFunFinder(pm), cppStd);
   }

   private MissingMemFunFinder getMissingMemFunFinder(final IProgressMonitor pm) {
      switch (testDouble.getPolymorphismKind()) {
      case StaticPoly:
         return new StaticPolyMissingMemFunFinder(getProject(), getIndex());
      case SubTypePoly:
         return new SubtypeMissingMemFunFinder(getProject(), getIndex(), pm);
      default:
         throw new ILTISException("Unsupported polymorphism kind").rethrowUnchecked();
      }
   }
}
