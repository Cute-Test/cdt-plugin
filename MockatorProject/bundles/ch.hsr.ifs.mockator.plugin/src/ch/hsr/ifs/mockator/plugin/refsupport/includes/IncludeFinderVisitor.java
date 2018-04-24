package ch.hsr.ifs.mockator.plugin.refsupport.includes;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
      if (includeName.isEmpty()) { return true; }

      final IASTPreprocessorStatement[] prepStmts = tu.getAllPreprocessorStatements();
      return !Arrays.asList(prepStmts).stream().filter(new IncludeStatementFinder(includeName)).collect(Collectors.toList()).isEmpty();
   }

   private static class IncludeStatementFinder implements Predicate<IASTPreprocessorStatement> {

      private final String includeName;

      public IncludeStatementFinder(final String includeName) {
         this.includeName = includeName;
      }

      @Override
      public boolean test(final IASTPreprocessorStatement stmt) {
         if (!(stmt instanceof IASTPreprocessorIncludeStatement)) { return false; }

         final IASTName foundIncludeName = ((IASTPreprocessorIncludeStatement) stmt).getName();
         return includeName.equals(foundIncludeName.toString());
      }
   }
}
