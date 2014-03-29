package ch.hsr.ifs.mockator.plugin.testdouble.creation;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

public abstract class AbstractCreateTestDoubleRefactoring extends MockatorRefactoring {

  public AbstractCreateTestDoubleRefactoring(ICElement cElement, ITextSelection selection,
      ICProject cProject) {
    super(cElement, selection, cProject);
  }

  @Override
  public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException,
      OperationCanceledException {
    @SuppressWarnings("restriction")
    RefactoringStatus status = super.checkInitialConditions(pm);

    for (IASTName optName : checkSelectedNameIsInFunction(status, pm)) {
      IBinding binding = optName.resolveBinding();

      if (!(binding instanceof IProblemBinding)) {
        status.addFatalError("Selected name refers to an existing entity");
      }
    }
    return status;
  }

  protected void insertBeforeCurrentStmt(IASTDeclarationStatement testDouble,
      IASTTranslationUnit ast, ASTRewrite rewriter) {
    for (IASTStatement optStmt : findFirstSelectedStmt(ast)) {
      rewriter.insertBefore(optStmt.getParent(), optStmt, testDouble, null);
    }
  }

  private Maybe<IASTStatement> findFirstSelectedStmt(IASTTranslationUnit ast) {
    IASTStatement stmt = AstUtil.getAncestorOfType(getSelectedNode(ast), IASTStatement.class);
    return maybe(stmt);
  }

  protected ICPPASTCompositeTypeSpecifier createNewTestDoubleClass(String name) {
    IASTName className = nodeFactory.newName(name.toCharArray());
    int structClassType = IASTCompositeTypeSpecifier.k_struct;
    ICPPASTCompositeTypeSpecifier newClass =
        nodeFactory.newCompositeTypeSpecifier(structClassType, className);
    return newClass;
  }

  @Override
  public String getDescription() {
    return I18N.CreateTestDoubleRefactoringDesc;
  }
}
