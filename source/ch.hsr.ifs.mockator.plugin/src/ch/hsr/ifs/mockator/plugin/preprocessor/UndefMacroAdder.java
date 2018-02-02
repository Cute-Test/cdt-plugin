package ch.hsr.ifs.mockator.plugin.preprocessor;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.SPACE;
import static ch.hsr.ifs.iltis.cpp.util.CPPNameConstants.UNDEF_DIRECTIVE;
import static ch.hsr.ifs.mockator.plugin.base.util.PlatformUtil.SYSTEM_NEW_LINE;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTLiteralNode;


@SuppressWarnings("restriction")
class UndefMacroAdder {

   private final ASTRewrite          rewriter;
   private final IASTTranslationUnit ast;
   private final IASTNode            insertionPoint;

   public UndefMacroAdder(final IASTTranslationUnit tu, final ASTRewrite rewriter, final IASTNode insertionPoint) {
      ast = tu;
      this.rewriter = rewriter;
      this.insertionPoint = insertionPoint;
   }

   public void addUndefMacro(final String funName) {
      rewriter.insertBefore(ast, insertionPoint, getUndefMacro(funName), null);
   }

   private static ASTLiteralNode getUndefMacro(final String funName) {
      final String undef = UNDEF_DIRECTIVE + SPACE + funName + SYSTEM_NEW_LINE;
      return new ASTLiteralNode(undef);
   }
}
