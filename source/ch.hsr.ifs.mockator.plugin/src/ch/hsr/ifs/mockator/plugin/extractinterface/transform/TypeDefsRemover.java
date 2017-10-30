package ch.hsr.ifs.mockator.plugin.extractinterface.transform;

import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;


public class TypeDefsRemover implements F1V<ExtractInterfaceContext> {

   @Override
   public void apply(ExtractInterfaceContext context) {
      ASTRewrite rewriter = context.getRewriterFor(context.getTuOfChosenClass());

      for (IASTSimpleDeclaration typeDef : context.getTypeDefDecls()) {
         rewriter.remove(typeDef, null);
      }
   }
}
