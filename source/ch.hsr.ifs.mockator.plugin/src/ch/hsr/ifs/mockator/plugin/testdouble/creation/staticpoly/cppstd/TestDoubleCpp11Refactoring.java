package ch.hsr.ifs.mockator.plugin.testdouble.creation.staticpoly.cppstd;

import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.mockator.plugin.testdouble.creation.AbstractCreateTestDoubleRefactoring;

@SuppressWarnings("restriction")
class TestDoubleCpp11Refactoring extends AbstractCreateTestDoubleRefactoring {

  public TestDoubleCpp11Refactoring(ICElement cElement, ITextSelection selection, ICProject cProject) {
    super(cElement, selection, cProject);
  }

  @Override
  protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
      throws CoreException, OperationCanceledException {
    IASTTranslationUnit ast = getAST(tu, pm);
    ASTRewrite rewriter = createRewriter(collector, ast);
    insertBeforeCurrentStmt(createNewClassDefinition(ast), ast, rewriter);
  }

  private IASTDeclarationStatement createNewClassDefinition(IASTTranslationUnit ast) {
    String newClassName = getSelectedName(ast).get().toString();
    ICPPASTCompositeTypeSpecifier testDouble = createNewTestDoubleClass(newClassName);
    return nodeFactory.newDeclarationStatement(nodeFactory.newSimpleDeclaration(testDouble));
  }
}
