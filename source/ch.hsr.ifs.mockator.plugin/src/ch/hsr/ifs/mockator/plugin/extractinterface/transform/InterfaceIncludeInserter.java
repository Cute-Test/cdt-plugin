package ch.hsr.ifs.mockator.plugin.extractinterface.transform;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.AstIncludeNode;


public class InterfaceIncludeInserter implements F1V<ExtractInterfaceContext> {

   @Override
   public void apply(ExtractInterfaceContext context) {
      IASTTranslationUnit tuOfChosenClass = context.getTuOfChosenClass();
      ASTRewrite rewriter = context.getRewriterFor(tuOfChosenClass);
      AstIncludeNode include = new AstIncludeNode(context.getInterfaceFilePath().toString());
      include.insertInTu(tuOfChosenClass, rewriter);
   }
}
