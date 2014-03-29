package ch.hsr.ifs.mockator.plugin.refsupport.utils;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.map;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;

import ch.hsr.ifs.mockator.plugin.base.functional.F1;
import ch.hsr.ifs.mockator.plugin.base.misc.CastHelper;

@SuppressWarnings("restriction")
public class CtorArgumentsCopier {
  private final ICPPASTConstructorInitializer ctorInitializer;

  public CtorArgumentsCopier(ICPPASTConstructorInitializer initializer) {
    this.ctorInitializer = initializer;
  }

  public Collection<IASTInitializerClause> getArguments() {
    return map(list(ctorInitializer.getArguments()), new InitializerCopyFunction());
  }

  private static class InitializerCopyFunction implements
      F1<IASTInitializerClause, IASTInitializerClause> {
    @Override
    public IASTInitializerClause apply(IASTInitializerClause clause) {
      return copyPreservingNameBindings(clause);
    }
  }

  private static <T extends IASTNode> T copyPreservingNameBindings(T node) {
    T copy = CastHelper.unsecureCast(node.copy());
    preserveNameBinding(copy, node);
    return copy;
  }

  private static boolean isValidType(IBinding binding) {
    return isValidType((Object) binding);
  }

  private static boolean isValidType(Object o) {
    return !(o instanceof IProblemBinding || o instanceof ICPPUnknownBinding || o instanceof IProblemType);
  }

  private static void preserveNameBinding(IASTNode copy, IASTNode original) {
    if (copy instanceof IASTName) {
      IASTName copiedName = (IASTName) copy;
      IASTName originalName = (IASTName) original;
      IBinding resolvedBinding = originalName.resolveBinding();

      if (isValidType(resolvedBinding)) {
        copiedName.setBinding(resolvedBinding);
      }
    }

    IASTNode[] children = copy.getChildren();
    for (int i = 0; i < children.length; ++i) {
      preserveNameBinding(children[i], original.getChildren()[i]);
    }
  }
}
