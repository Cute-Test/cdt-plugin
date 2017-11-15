package ch.hsr.ifs.mockator.plugin.linker.wrapfun.gnuoption;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import ch.hsr.ifs.iltis.core.exception.ILTISException;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;



public class GnuOptionChecker extends AbstractIndexAstChecker {

   public static final String WRAP_FUNCTION_PROBLEM_ID = "ch.hsr.ifs.mockator.WrapFunctionProblem";

   @Override
   public void processAst(final IASTTranslationUnit ast) {
      for (final IASTPreprocessorStatement pStmt : ast.getAllPreprocessorStatements()) {
         if (!(pStmt instanceof IASTPreprocessorIfdefStatement)) {
            continue;
         }

         final IASTPreprocessorIfdefStatement ifDefStmt = (IASTPreprocessorIfdefStatement) pStmt;

         if (isWrapMacroCheck(ifDefStmt)) {
            reportProblem(WRAP_FUNCTION_PROBLEM_ID, ifDefStmt, unpackName(getMacroCheckName(ifDefStmt)));
         }
      }
   }

   private static boolean isWrapMacroCheck(final IASTPreprocessorIfdefStatement ifDefStmt) {
      return getMacroCheckName(ifDefStmt).startsWith(MockatorConstants.WRAP_MACRO_PREFIX);
   }

   private static String getMacroCheckName(final IASTPreprocessorIfdefStatement ifDefStmt) {
      return ifDefStmt.getMacroReference().toString();
   }

   private static String unpackName(final String wrapFunName) {
      final int idx = wrapFunName.indexOf(MockatorConstants.WRAP_MACRO_PREFIX);
      ILTISException.Unless.isTrue(idx >= 0, "Invalid wrapped function");
      return wrapFunName.substring(idx + MockatorConstants.WRAP_MACRO_PREFIX.length());
   }
}
