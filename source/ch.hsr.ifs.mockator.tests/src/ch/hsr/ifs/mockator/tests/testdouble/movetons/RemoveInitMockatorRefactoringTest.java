package ch.hsr.ifs.mockator.tests.testdouble.movetons;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.NodeContainer;
import org.eclipse.ltk.core.refactoring.RefactoringContext;

import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.testdouble.movetons.RemoveInitMockatorRefactoring;
import ch.hsr.ifs.mockator.tests.MockatorRefactoringTest;

@SuppressWarnings("restriction")
public class RemoveInitMockatorRefactoringTest extends MockatorRefactoringTest {

  @Override
  protected MockatorRefactoring createRefactoring() {
    try {
      return new RemoveInitMockatorRefactoring(getActiveDocument(), getActiveCElement(), selection,
          cproject);
    } catch (Exception e) {
      fail(e.getMessage());
    }
    return null;
  }

  @Override
  protected void simulateUserInput(RefactoringContext context) {
    CRefactoringContext ccontext = (CRefactoringContext) context;
    ICPPASTFunctionDefinition testFunction = getTestFunctionIn(getAst(ccontext));
    ((RemoveInitMockatorRefactoring) context.getRefactoring()).setTestFunction(testFunction);
  }

  private static ICPPASTFunctionDefinition getTestFunctionIn(IASTTranslationUnit ast) {
    FunFinderVisitor funVisitor = new FunFinderVisitor();
    ast.accept(funVisitor);
    return funVisitor.getFunction();
  }

  private static class FunFinderVisitor extends ASTVisitor {
    private final NodeContainer container;

    {
      shouldVisitDeclarators = true;
    }

    public FunFinderVisitor() {
      container = new NodeContainer();
    }

    public ICPPASTFunctionDefinition getFunction() {
      return (ICPPASTFunctionDefinition) container.getNodesToWrite().get(0);
    }

    @Override
    public int visit(IASTDeclarator decl) {
      ICPPASTFunctionDefinition function =
          AstUtil.getAncestorOfType(decl, ICPPASTFunctionDefinition.class);

      if (function != null && isTestFunction(function)) {
        container.add(function);
        return PROCESS_ABORT;
      }

      return PROCESS_CONTINUE;
    }

    private static boolean isTestFunction(ICPPASTFunctionDefinition function) {
      return function.getDeclarator().getName().toString().startsWith("test");
    }
  }
}
