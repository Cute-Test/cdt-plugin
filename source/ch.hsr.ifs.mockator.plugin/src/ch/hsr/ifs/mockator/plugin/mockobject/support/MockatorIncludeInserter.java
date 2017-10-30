package ch.hsr.ifs.mockator.plugin.mockobject.support;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.mockator.plugin.refsupport.includes.AstIncludeNode;


// Adds the include directive for the mockator header:
// #include "mockator.h"
public class MockatorIncludeInserter {

   private static final String       MOCKATOR_HEADER = "mockator.h";
   private final IASTTranslationUnit ast;

   public MockatorIncludeInserter(IASTTranslationUnit ast) {
      this.ast = ast;
   }

   public void insertWith(ASTRewrite rewriter) {
      AstIncludeNode include = new AstIncludeNode(MOCKATOR_HEADER);
      include.insertInTu(ast, rewriter);
   }
}
