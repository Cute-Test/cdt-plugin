package ch.hsr.ifs.mockator.plugin.testdouble.support;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.cdt.codan.core.model.ICheckerWithPreferences;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;

import ch.hsr.ifs.iltis.cpp.core.ast.checker.SimpleChecker;
import ch.hsr.ifs.iltis.cpp.core.ast.checker.VisitorReport;
import ch.hsr.ifs.iltis.cpp.core.ast.checker.helper.IProblemId;
import ch.hsr.ifs.iltis.cpp.core.ast.visitor.SimpleVisitor;

import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;
import ch.hsr.ifs.mockator.plugin.project.properties.FunctionsToAnalyze;


public abstract class TestFunctionChecker extends SimpleChecker<ProblemId> implements ICheckerWithPreferences {

   @Override
   protected SimpleVisitor<ProblemId, Consumer<VisitorReport<ProblemId>>> createVisitor() {
      return new SimpleVisitor<ProblemId, Consumer<VisitorReport<ProblemId>>>(TestFunctionChecker.this, this::processTestFunction) {

         {
            shouldVisitDeclarations = true;
         }

         @Override
         public int visit(final IASTDeclaration declaration) {
            if (!(declaration instanceof IASTFunctionDefinition)) return PROCESS_CONTINUE;

            final IASTFunctionDefinition candidate = (IASTFunctionDefinition) declaration;

            if (isValidTestFunction(candidate)) {
            	arguments.get(0).accept(new VisitorReport<ProblemId>(getProblemId(), candidate));
            }

            return PROCESS_SKIP;
         }

		@Override
		public Set<? extends IProblemId> getProblemIds() {
			return EnumSet.of(getProblemId());
		}
      };
   }

   protected abstract ProblemId getProblemId();

   private boolean isValidTestFunction(final IASTFunctionDefinition function) {
      return FunctionsToAnalyze.fromProjectSettings(getProject()).shouldConsider(function);
   }

   protected abstract void processTestFunction(VisitorReport<ProblemId> result /* IASTFunctionDefinition function */);
}
