package ch.hsr.ifs.mockator.plugin.testdouble.creation.staticpoly;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;

import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.testdouble.support.TestFunctionChecker;


public class MissingTestDoubleStaticPolyChecker extends TestFunctionChecker {

   public static final String MISSING_TEST_DOUBLE_STATICPOLY_PROBLEM_ID = "ch.hsr.ifs.mockator.MissingTestDoublStaticPolyProblem";

   @Override
   protected void processTestFunction(IASTFunctionDefinition function) {
      function.accept(new OnEachFunction());
   }

   private class OnEachFunction extends ASTVisitor {

      {
         shouldVisitNames = true;
      }

      @Override
      public int visit(IASTName name) {
         IBinding binding = name.resolveBinding();

         if (binding instanceof IProblemBinding) {
            if (isPartOfTemplateId(name) && isTypeId(name)) {
               mark(name);
            }
         }

         return PROCESS_CONTINUE;
      }
   };

   private void mark(IASTName name) {
      reportProblem(MISSING_TEST_DOUBLE_STATICPOLY_PROBLEM_ID, name, name.toString());
   }

   private static boolean isTypeId(IASTName name) {
      ICPPASTNamedTypeSpecifier nts = AstUtil.getAncestorOfType(name, ICPPASTNamedTypeSpecifier.class);
      return nts != null && AstUtil.getAncestorOfType(nts, ICPPASTTypeId.class) != null;
   }

   private static boolean isPartOfTemplateId(IASTName name) {
      return AstUtil.getAncestorOfType(name, ICPPASTTemplateId.class) != null;
   }
}
