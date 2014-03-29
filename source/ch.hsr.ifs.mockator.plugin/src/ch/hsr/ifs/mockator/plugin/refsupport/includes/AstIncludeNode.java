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
  private final String includeName;

  public AstIncludeNode(String includeName) {
    super(getIncludeCode(includeName, false));
    this.includeName = includeName;
  }

  public AstIncludeNode(String includeName, boolean isSystemInclude) {
    super(getIncludeCode(includeName, isSystemInclude));
    this.includeName = includeName;
  }

  public AstIncludeNode(IASTPreprocessorIncludeStatement include) {
    super(getIncludeCode(include));
    this.includeName = getIncludeName(include);
  }

  private static String getIncludeCode(IASTPreprocessorIncludeStatement include) {
    return getIncludeCode(getIncludeName(include), include.isSystemInclude());
  }

  private static String getIncludeName(IASTPreprocessorIncludeStatement include) {
    return include.getName().toString();
  }

  private static String getIncludeCode(String include, boolean isSystemInclude) {
    if (isSystemInclude)
      return getSystemInclude(include);
    else
      return getUserInclude(include);
  }

  private static String getSystemInclude(String include) {
    return String.format("#include <%s>", include) + NEW_LINE;
  }

  private static String getUserInclude(String include) {
    return String.format("#include \"%s\"", include) + NEW_LINE;
  }

  public void insertInTu(IASTTranslationUnit ast, ASTRewrite rewriter) {
    IncludeFinderVisitor finder = new IncludeFinderVisitor(ast);

    if (!finder.hasIncludeStatement(includeName)) {
      rewriter.insertBefore(ast, getInsertionPoint(ast), this, null);
    }
  }

  private static IASTNode getInsertionPoint(IASTTranslationUnit ast) {
    IASTNode[] children = ast.getChildren();

    if (children == null || children.length == 0)
      return null;

    return children[0];
  }
}
