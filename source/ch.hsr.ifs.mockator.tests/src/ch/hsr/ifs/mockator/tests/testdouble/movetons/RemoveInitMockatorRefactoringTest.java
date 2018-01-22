package ch.hsr.ifs.mockator.tests.testdouble.movetons;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.internal.ui.refactoring.NodeContainer;
import org.eclipse.ltk.core.refactoring.RefactoringContext;

import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.wrappers.CRefactoringContext;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.mockator.plugin.testdouble.movetons.RemoveInitMockatorRefactoring;
import ch.hsr.ifs.mockator.tests.MockatorRefactoringTest;


@SuppressWarnings("restriction")
public class RemoveInitMockatorRefactoringTest extends MockatorRefactoringTest {

   @Override
   protected MockatorRefactoring createRefactoring() {
      try {
         return new RemoveInitMockatorRefactoring(getActiveDocument(), getActiveCElement(), selection, cproject);
      } catch (final Exception e) {
         fail(e.getMessage());
      }
      return null;
   }

   @Override
   protected void simulateUserInput(final RefactoringContext context) {
      final CRefactoringContext ccontext = CRefactoringContext.wrapp(context);
      final ICPPASTFunctionDefinition testFunction = getTestFunctionIn(getAst(ccontext));
      ((RemoveInitMockatorRefactoring) context.getRefactoring()).setTestFunction(testFunction);
   }

   private static ICPPASTFunctionDefinition getTestFunctionIn(final IASTTranslationUnit ast) {
      final FunFinderVisitor funVisitor = new FunFinderVisitor();
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
      public int visit(final IASTDeclarator decl) {
         final ICPPASTFunctionDefinition function = ASTUtil.getAncestorOfType(decl, ICPPASTFunctionDefinition.class);

         if (function != null && isTestFunction(function)) {
            container.add(function);
            return PROCESS_ABORT;
         }

         return PROCESS_CONTINUE;
      }

      private static boolean isTestFunction(final ICPPASTFunctionDefinition function) {
         return function.getDeclarator().getName().toString().startsWith("test");
      }
   }
}
