package ch.hsr.ifs.cute.mockator.extractinterface.preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.cute.mockator.extractinterface.context.ExtractInterfaceContext;


public class ForwardDeclCollector implements Consumer<ExtractInterfaceContext> {

   @Override
   public void accept(final ExtractInterfaceContext context) {
      final Collection<IASTSimpleDeclaration> fwdDecls = getClassFwdDecls(context.getTuOfChosenClass());
      context.setClassFwdDecls(fwdDecls);
   }

   private static Collection<IASTSimpleDeclaration> getClassFwdDecls(final IASTTranslationUnit ast) {
      final List<IASTSimpleDeclaration> fwdDecls = new ArrayList<>();
      ast.accept(new ASTVisitor() {

         {
            shouldVisitDeclarations = true;
         }

         @Override
         public int visit(final IASTDeclaration decl) {
            if (decl instanceof IASTSimpleDeclaration) {
               final ICPPASTElaboratedTypeSpecifier forwardDecl = CPPVisitor.findChildWithType(decl, ICPPASTElaboratedTypeSpecifier.class).orElse(
                     null);

               if (forwardDecl != null) {
                  fwdDecls.add((IASTSimpleDeclaration) decl);
               }
            }

            return PROCESS_CONTINUE;
         }
      });
      return fwdDecls;
   }
}
