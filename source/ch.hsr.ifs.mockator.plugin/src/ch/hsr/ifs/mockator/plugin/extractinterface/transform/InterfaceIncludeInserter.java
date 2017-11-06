package ch.hsr.ifs.mockator.plugin.extractinterface.transform;

import java.util.function.Consumer;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.AstIncludeNode;


public class InterfaceIncludeInserter implements Consumer<ExtractInterfaceContext> {

   @Override
   public void accept(final ExtractInterfaceContext context) {
      final IASTTranslationUnit tuOfChosenClass = context.getTuOfChosenClass();
      final ASTRewrite rewriter = context.getRewriterFor(tuOfChosenClass);
      final AstIncludeNode include = new AstIncludeNode(context.getInterfaceFilePath().toString());
      include.insertInTu(tuOfChosenClass, rewriter);
   }
}
