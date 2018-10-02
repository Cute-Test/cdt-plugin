package ch.hsr.ifs.cute.mockator.linker.wrapfun.gnuoption;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import ch.hsr.ifs.cute.mockator.MockatorConstants;
import ch.hsr.ifs.cute.mockator.ids.IdHelper.ProblemId;
import ch.hsr.ifs.cute.mockator.infos.GnuOptionInfo;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;
import ch.hsr.ifs.iltis.cpp.core.wrappers.AbstractIndexAstChecker;


public class GnuOptionChecker extends AbstractIndexAstChecker {

   @Override
   public void processAst(final IASTTranslationUnit ast) {
      for (final IASTPreprocessorStatement pStmt : ast.getAllPreprocessorStatements()) {
         if (!(pStmt instanceof IASTPreprocessorIfdefStatement)) {
            continue;
         }

         final IASTPreprocessorIfdefStatement ifDefStmt = (IASTPreprocessorIfdefStatement) pStmt;

         if (isWrapMacroCheck(ifDefStmt)) {
            reportProblem(ProblemId.WRAP_FUNCTION, ifDefStmt, new GnuOptionInfo(unpackName(getMacroCheckName(ifDefStmt))));
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
      ILTISException.Unless.isTrue("Invalid wrapped function", idx >= 0);
      return wrapFunName.substring(idx + MockatorConstants.WRAP_MACRO_PREFIX.length());
   }
}
