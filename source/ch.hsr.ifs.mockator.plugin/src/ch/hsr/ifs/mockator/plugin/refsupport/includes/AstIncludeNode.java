package ch.hsr.ifs.mockator.plugin.refsupport.includes;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTLiteralNode;

import ch.hsr.ifs.mockator.plugin.base.util.PlatformUtil;


@SuppressWarnings("restriction")
public class AstIncludeNode extends ASTLiteralNode {

   private static final String NEW_LINE = PlatformUtil.toSystemNewLine("%n");
   private final String        includeName;

   public AstIncludeNode(final String includeName) {
      super(getIncludeCode(includeName, false));
      this.includeName = includeName;
   }

   public AstIncludeNode(final String includeName, final boolean isSystemInclude) {
      super(getIncludeCode(includeName, isSystemInclude));
      this.includeName = includeName;
   }

   public AstIncludeNode(final IASTPreprocessorIncludeStatement include) {
      super(getIncludeCode(include));
      includeName = getIncludeName(include);
   }

   private static String getIncludeCode(final IASTPreprocessorIncludeStatement include) {
      return getIncludeCode(getIncludeName(include), include.isSystemInclude());
   }

   private static String getIncludeName(final IASTPreprocessorIncludeStatement include) {
      return include.getName().toString();
   }

   private static String getIncludeCode(final String include, final boolean isSystemInclude) {
      if (isSystemInclude) return getSystemInclude(include);
      else return getUserInclude(include);
   }

   private static String getSystemInclude(final String include) {
      return String.format("#include <%s>", include) + NEW_LINE;
   }

   private static String getUserInclude(final String include) {
      return String.format("#include \"%s\"", include) + NEW_LINE;
   }

   public void insertInTu(final IASTTranslationUnit ast, final ASTRewrite rewriter) {
      final IncludeFinderVisitor finder = new IncludeFinderVisitor(ast);

      if (!finder.hasIncludeStatement(includeName)) {
         rewriter.insertBefore(ast, getInsertionPoint(ast), this, null);
      }
   }

   private static IASTNode getInsertionPoint(final IASTTranslationUnit ast) {
      final IASTNode[] children = ast.getChildren();

      if (children == null || children.length == 0) return null;

      return children[0];
   }
}
