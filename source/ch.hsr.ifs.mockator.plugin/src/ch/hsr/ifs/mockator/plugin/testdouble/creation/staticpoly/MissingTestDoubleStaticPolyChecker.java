package ch.hsr.ifs.mockator.plugin.testdouble.creation.staticpoly;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;

import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;
import ch.hsr.ifs.mockator.plugin.testdouble.support.TestFunctionChecker;


public class MissingTestDoubleStaticPolyChecker extends TestFunctionChecker {

   public static final String MISSING_TEST_DOUBLE_STATICPOLY_PROBLEM_ID = "ch.hsr.ifs.mockator.MissingTestDoublStaticPolyProblem";

   @Override
   protected void processTestFunction(final IASTFunctionDefinition function) {
      function.accept(new OnEachFunction());
   }

   private class OnEachFunction extends ASTVisitor {

      {
         shouldVisitNames = true;
      }

      @Override
      public int visit(final IASTName name) {
         final IBinding binding = name.resolveBinding();

         if (binding instanceof IProblemBinding) {
            if (isPartOfTemplateId(name) && isTypeId(name)) {
               mark(name);
            }
         }

         return PROCESS_CONTINUE;
      }
   };

   private void mark(final IASTName name) {
      reportProblem(MISSING_TEST_DOUBLE_STATICPOLY_PROBLEM_ID, name, name.toString());
   }

   private static boolean isTypeId(final IASTName name) {
      final ICPPASTNamedTypeSpecifier nts = ASTUtil.getAncestorOfType(name, ICPPASTNamedTypeSpecifier.class);
      return nts != null && ASTUtil.getAncestorOfType(nts, ICPPASTTypeId.class) != null;
   }

   private static boolean isPartOfTemplateId(final IASTName name) {
      return ASTUtil.getAncestorOfType(name, ICPPASTTemplateId.class) != null;
   }
}
