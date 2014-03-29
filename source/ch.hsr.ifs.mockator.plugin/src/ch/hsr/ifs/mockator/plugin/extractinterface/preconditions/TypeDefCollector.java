package ch.hsr.ifs.mockator.plugin.extractinterface.preconditions;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;

public class TypeDefCollector implements F1V<ExtractInterfaceContext> {

  @Override
  public void apply(ExtractInterfaceContext context) {
    Collection<IASTSimpleDeclaration> fwdDecls = findTypeDefDecls(context.getTuOfChosenClass());
    context.setTypeDefDecls(fwdDecls);
  }

  private static Collection<IASTSimpleDeclaration> findTypeDefDecls(IASTTranslationUnit ast) {
    final List<IASTSimpleDeclaration> typeDefs = list();
    ast.accept(new ASTVisitor() {
      {
        shouldVisitDeclarations = true;
      }

      @Override
      public int visit(IASTDeclaration declaration) {
        if (declaration instanceof IASTSimpleDeclaration) {
          IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) declaration;

          if (isTypeDef(simpleDecl.getDeclSpecifier())) {
            typeDefs.add((IASTSimpleDeclaration) declaration);
          }
        }
        return PROCESS_CONTINUE;
      }
    });
    return typeDefs;
  }

  private static boolean isTypeDef(IASTDeclSpecifier typeSpec) {
    return typeSpec instanceof IASTNamedTypeSpecifier
        && typeSpec.getStorageClass() == IASTDeclSpecifier.sc_typedef;
  }
}
