package ch.hsr.ifs.mockator.plugin.testdouble.support;

import org.eclipse.cdt.codan.core.model.ICheckerWithPreferences;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;

import ch.hsr.ifs.iltis.cpp.core.ast.checker.CheckerResult;
import ch.hsr.ifs.iltis.cpp.core.ast.checker.SimpleChecker;
import ch.hsr.ifs.iltis.cpp.core.ast.visitor.SimpleVisitor;

import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;
import ch.hsr.ifs.mockator.plugin.project.properties.FunctionsToAnalyze;


public abstract class TestFunctionChecker extends SimpleChecker<ProblemId> implements ICheckerWithPreferences {

   @Override
   protected ASTVisitor getVisitor() {
      return new SimpleVisitor<ProblemId>(this::processTestFunction) {

         {
            shouldVisitDeclarations = true;
         }

         @Override
         public int visit(final IASTDeclaration declaration) {
            if (!(declaration instanceof IASTFunctionDefinition)) return PROCESS_CONTINUE;

            final IASTFunctionDefinition candidate = (IASTFunctionDefinition) declaration;

            if (isValidTestFunction(candidate)) {
               report(getProblemId(), declaration);
            }

            return PROCESS_SKIP;
         }
      };
   }

   protected abstract ProblemId getProblemId();

   private boolean isValidTestFunction(final IASTFunctionDefinition function) {
      return FunctionsToAnalyze.fromProjectSettings(getProject()).shouldConsider(function);
   }

   protected abstract void processTestFunction(CheckerResult<ProblemId> result /* IASTFunctionDefinition function */);
}
