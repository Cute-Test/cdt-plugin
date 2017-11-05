package ch.hsr.ifs.mockator.plugin.refsupport.includes;

import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.filter;

import java.util.function.Function;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

class IncludeFinderVisitor {

  private final IASTTranslationUnit tu;

  public IncludeFinderVisitor(final IASTTranslationUnit tu) {
    this.tu = tu;
  }

  public boolean hasIncludeStatement(final String includeName) {
    if (includeName.isEmpty())
      return true;

    final IASTPreprocessorStatement[] prepStmts = tu.getAllPreprocessorStatements();
    return !filter(prepStmts, new IncludeStatementFinder(includeName)).isEmpty();
  }

  private static class IncludeStatementFinder implements Function<IASTPreprocessorStatement, Boolean> {

    private final String includeName;

    public IncludeStatementFinder(final String includeName) {
      this.includeName = includeName;
    }

    @Override
    public Boolean apply(final IASTPreprocessorStatement stmt) {
      if (!(stmt instanceof IASTPreprocessorIncludeStatement))
        return false;

      final IASTName foundIncludeName = ((IASTPreprocessorIncludeStatement) stmt).getName();
      return includeName.equals(foundIncludeName.toString());
    }
  }
}
