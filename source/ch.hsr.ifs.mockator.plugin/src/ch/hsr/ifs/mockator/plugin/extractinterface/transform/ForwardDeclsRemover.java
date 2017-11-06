package ch.hsr.ifs.mockator.plugin.extractinterface.transform;

import java.util.function.Consumer;

import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;


public class ForwardDeclsRemover implements Consumer<ExtractInterfaceContext> {

   @Override
   public void accept(final ExtractInterfaceContext context) {
      final ASTRewrite rewriter = context.getRewriterFor(context.getTuOfChosenClass());

      for (final IASTSimpleDeclaration fwdDecl : context.getClassFwdDecls()) {
         rewriter.remove(fwdDecl, null);
      }
   }
}
