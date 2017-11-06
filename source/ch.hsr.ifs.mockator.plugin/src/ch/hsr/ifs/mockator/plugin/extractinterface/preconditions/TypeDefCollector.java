package ch.hsr.ifs.mockator.plugin.extractinterface.preconditions;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;

public class TypeDefCollector implements Consumer<ExtractInterfaceContext> {

  @Override
  public void accept(final ExtractInterfaceContext context) {
    final Collection<IASTSimpleDeclaration> fwdDecls = findTypeDefDecls(context.getTuOfChosenClass());
    context.setTypeDefDecls(fwdDecls);
  }

  private static Collection<IASTSimpleDeclaration> findTypeDefDecls(final IASTTranslationUnit ast) {
    final List<IASTSimpleDeclaration> typeDefs = list();
    ast.accept(new ASTVisitor() {

      {
        shouldVisitDeclarations = true;
      }

      @Override
      public int visit(final IASTDeclaration declaration) {
        if (declaration instanceof IASTSimpleDeclaration) {
          final IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) declaration;

          if (isTypeDef(simpleDecl.getDeclSpecifier())) {
            typeDefs.add((IASTSimpleDeclaration) declaration);
          }
        }
        return PROCESS_CONTINUE;
      }
    });
    return typeDefs;
  }

  private static boolean isTypeDef(final IASTDeclSpecifier typeSpec) {
    return typeSpec instanceof IASTNamedTypeSpecifier && typeSpec.getStorageClass() == IASTDeclSpecifier.sc_typedef;
  }
}
