package ch.hsr.ifs.cute.mockator.testdouble.creation.staticpoly;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;

import ch.hsr.ifs.iltis.cpp.core.ast.checker.VisitorReport;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.cute.mockator.ids.IdHelper.ProblemId;
import ch.hsr.ifs.cute.mockator.infos.MissingTestDoubleStaticPolyInfo;
import ch.hsr.ifs.cute.mockator.testdouble.support.TestFunctionChecker;


public class MissingTestDoubleStaticPolyChecker extends TestFunctionChecker {

   @Override
   protected void processTestFunction(final VisitorReport<ProblemId> result) {
      ((IASTFunctionDefinition) result.getNode()).accept(new OnEachFunction());
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
            	addNodeForReporting(getProblemId(), name, new MissingTestDoubleStaticPolyInfo(name.toString()));
            }
         }

         return PROCESS_CONTINUE;
      }

   };

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
