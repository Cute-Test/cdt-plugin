package ch.hsr.ifs.mockator.plugin.testdouble.support;

import org.eclipse.cdt.codan.core.model.ICheckerWithPreferences;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;

import ch.hsr.ifs.mockator.plugin.project.properties.FunctionsToAnalyze;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorIndexAstChecker;


public abstract class TestFunctionChecker extends MockatorIndexAstChecker implements ICheckerWithPreferences {

   @Override
   protected ASTVisitor getAstVisitor() {
      return new ASTVisitor() {

         {
            shouldVisitDeclarations = true;
         }

         @Override
         public int visit(final IASTDeclaration declaration) {
            if (!(declaration instanceof IASTFunctionDefinition)) return PROCESS_CONTINUE;

            final IASTFunctionDefinition candidate = (IASTFunctionDefinition) declaration;

            if (isValidTestFunction(candidate)) {
               processTestFunction((IASTFunctionDefinition) declaration);
            }

            return PROCESS_SKIP;
         }
      };
   }

   private boolean isValidTestFunction(final IASTFunctionDefinition function) {
      return FunctionsToAnalyze.fromProjectSettings(getProject()).shouldConsider(function);
   }

   protected abstract void processTestFunction(IASTFunctionDefinition function);
}
