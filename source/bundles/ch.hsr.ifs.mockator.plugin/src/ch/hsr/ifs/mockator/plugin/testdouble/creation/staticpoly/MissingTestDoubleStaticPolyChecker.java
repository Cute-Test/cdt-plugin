package ch.hsr.ifs.mockator.plugin.testdouble.creation.staticpoly;

import java.util.function.Consumer;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;

import ch.hsr.ifs.iltis.cpp.core.ast.checker.CheckerResult;
import ch.hsr.ifs.iltis.cpp.core.ast.visitor.SimpleVisitor;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;
import ch.hsr.ifs.mockator.plugin.testdouble.support.TestFunctionChecker;


public class MissingTestDoubleStaticPolyChecker extends TestFunctionChecker {

   @Override
   protected void processTestFunction(final CheckerResult<ProblemId> result) {
      ((IASTFunctionDefinition) result.getNode()).accept(new OnEachFunction(this::mark));
   }

   private class OnEachFunction extends SimpleVisitor<ProblemId> {

      public OnEachFunction(final Consumer<CheckerResult<ProblemId>> callback) {
         super(callback);
      }

      {
         shouldVisitNames = true;
      }

      @Override
      public int visit(final IASTName name) {
         final IBinding binding = name.resolveBinding();

         if (binding instanceof IProblemBinding) {
            if (isPartOfTemplateId(name) && isTypeId(name)) {
               report(getProblemId(), name);
            }
         }

         return PROCESS_CONTINUE;
      }
   };

   private void mark(final CheckerResult<ProblemId> result) {
      addNodeForReporting(result, result.getNode().toString());
   }

   private static boolean isTypeId(final IASTName name) {
      final ICPPASTNamedTypeSpecifier nts = CPPVisitor.findAncestorWithType(name, ICPPASTNamedTypeSpecifier.class).orElse(null);
      return nts != null && CPPVisitor.findAncestorWithType(nts, ICPPASTTypeId.class).orElse(null) != null;
   }

   private static boolean isPartOfTemplateId(final IASTName name) {
      return CPPVisitor.findAncestorWithType(name, ICPPASTTemplateId.class).orElse(null) != null;
   }

   @Override
   protected ProblemId getProblemId() {
      return ProblemId.MISSING_TEST_DOUBLE_STATICPOLY;
   }
}
