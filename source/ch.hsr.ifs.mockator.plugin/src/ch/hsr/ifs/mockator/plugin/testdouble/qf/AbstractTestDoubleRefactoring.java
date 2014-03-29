package ch.hsr.ifs.mockator.plugin.testdouble.qf;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemFunFinder;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.StaticPolyMissingMemFunFinder;
import ch.hsr.ifs.mockator.plugin.incompleteclass.subtype.SubtypeMissingMemFunFinder;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.ClassPublicVisibilityInserter;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.TestDouble;

@SuppressWarnings("restriction")
public abstract class AbstractTestDoubleRefactoring extends MockatorRefactoring {
  protected CppStandard cppStd;
  protected TestDouble testDouble;

  public AbstractTestDoubleRefactoring(CppStandard cppStd, ICElement cElement,
      ITextSelection selection, ICProject project) {
    super(cElement, selection, project);
    this.cppStd = cppStd;
  }

  @Override
  public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException,
      OperationCanceledException {
    RefactoringStatus status = super.checkInitialConditions(pm);
    Maybe<ICPPASTCompositeTypeSpecifier> classInSelection = getClassInSelection(getAST(tu, pm));

    if (classInSelection.isNone()) {
      status.addFatalError("Could not find a class in the current selection");
    } else {
      testDouble = createTestDouble(classInSelection.get());
    }

    return status;
  }

  protected ClassPublicVisibilityInserter getPublicVisibilityInserter(ASTRewrite rewriter) {
    return new ClassPublicVisibilityInserter(testDouble.getKlass(), rewriter);
  }

  protected abstract TestDouble createTestDouble(ICPPASTCompositeTypeSpecifier selectedClass);

  protected Collection<? extends MissingMemberFunction> collectMissingMemFuns(IProgressMonitor pm) {
    return testDouble.collectMissingMemFuns(getMissingMemFunFinder(pm), cppStd);
  }

  private MissingMemFunFinder getMissingMemFunFinder(IProgressMonitor pm) {
    switch (testDouble.getPolymorphismKind()) {
      case StaticPoly:
        return new StaticPolyMissingMemFunFinder(project, getIndex());
      case SubTypePoly:
        return new SubtypeMissingMemFunFinder(project, getIndex(), pm);
      default:
        throw new MockatorException("Unsupported polymorphism kind");
    }
  }
}
