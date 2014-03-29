package ch.hsr.ifs.mockator.plugin.linker.wrapfun.gnuoption;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;

public class GnuOptionChecker extends AbstractIndexAstChecker {
  public static final String WRAP_FUNCTION_PROBLEM_ID = "ch.hsr.ifs.mockator.WrapFunctionProblem";

  @Override
  public void processAst(IASTTranslationUnit ast) {
    for (IASTPreprocessorStatement pStmt : ast.getAllPreprocessorStatements()) {
      if (!(pStmt instanceof IASTPreprocessorIfdefStatement)) {
        continue;
      }

      IASTPreprocessorIfdefStatement ifDefStmt = (IASTPreprocessorIfdefStatement) pStmt;

      if (isWrapMacroCheck(ifDefStmt)) {
        reportProblem(WRAP_FUNCTION_PROBLEM_ID, ifDefStmt, unpackName(getMacroCheckName(ifDefStmt)));
      }
    }
  }

  private static boolean isWrapMacroCheck(IASTPreprocessorIfdefStatement ifDefStmt) {
    return getMacroCheckName(ifDefStmt).startsWith(MockatorConstants.WRAP_MACRO_PREFIX);
  }

  private static String getMacroCheckName(IASTPreprocessorIfdefStatement ifDefStmt) {
    return ifDefStmt.getMacroReference().toString();
  }

  private static String unpackName(String wrapFunName) {
    int idx = wrapFunName.indexOf(MockatorConstants.WRAP_MACRO_PREFIX);
    Assert.isTrue(idx >= 0, "Invalid wrapped function");
    return wrapFunName.substring(idx + MockatorConstants.WRAP_MACRO_PREFIX.length());
  }
}
