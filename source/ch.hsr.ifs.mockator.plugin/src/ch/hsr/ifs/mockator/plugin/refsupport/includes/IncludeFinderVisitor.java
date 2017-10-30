package ch.hsr.ifs.mockator.plugin.refsupport.includes;

import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.filter;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import ch.hsr.ifs.mockator.plugin.base.functional.F1;


class IncludeFinderVisitor {

   private final IASTTranslationUnit tu;

   public IncludeFinderVisitor(IASTTranslationUnit tu) {
      this.tu = tu;
   }

   public boolean hasIncludeStatement(String includeName) {
      if (includeName.isEmpty()) return true;

      IASTPreprocessorStatement[] prepStmts = tu.getAllPreprocessorStatements();
      return !filter(prepStmts, new IncludeStatementFinder(includeName)).isEmpty();
   }

   private static class IncludeStatementFinder implements F1<IASTPreprocessorStatement, Boolean> {

      private final String includeName;

      public IncludeStatementFinder(String includeName) {
         this.includeName = includeName;
      }

      @Override
      public Boolean apply(IASTPreprocessorStatement stmt) {
         if (!(stmt instanceof IASTPreprocessorIncludeStatement)) return false;

         IASTName foundIncludeName = ((IASTPreprocessorIncludeStatement) stmt).getName();
         return includeName.equals(foundIncludeName.toString());
      }
   }
}
