package ch.hsr.ifs.mockator.plugin.testdouble.movetons;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;

@SuppressWarnings("restriction")
public class MoveTestDoubleToNsRefactoring extends MockatorRefactoring {
  private final CppStandard cppStd;
  private ICPPASTFunctionDefinition testFunction;

  public MoveTestDoubleToNsRefactoring(CppStandard cppStd, ICElement cElement,
      ITextSelection selection, ICProject cProject) {
    super(cElement, selection, cProject);
    this.cppStd = cppStd;
  }

  @Override
  public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException,
      OperationCanceledException {
    RefactoringStatus status = super.checkInitialConditions(pm);

    if (getClassInSelection(getAST(tu, pm)).isNone()) {
      status.addFatalError("Could not find a class in the current selection");
      return status;
    }

    checkSelectedNameIsInFunction(status, pm);
    return status;
  }

  @Override
  protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
      throws CoreException, OperationCanceledException {
    for (ICPPASTCompositeTypeSpecifier optClass : getClassInSelection(getAST(tu, pm))) {
      IASTTranslationUnit ast = getAST(tu, pm);
      ASTRewrite rewriter = createRewriter(collector, ast);
      testFunction = getParentFunction(optClass.getName());
      moveToNamespace(optClass, rewriter);
    }
  }

  private void moveToNamespace(ICPPASTCompositeTypeSpecifier optClass, ASTRewrite rewriter) {
    new TestDoubleToNsMover(rewriter, cppStd).moveToNamespace(optClass);
  }

  public ICPPASTFunctionDefinition getTestFunction() {
    return testFunction;
  }

  @Override
  public String getDescription() {
    return I18N.MoveTestDoubleToNsRefactoringDesc;
  }
}
