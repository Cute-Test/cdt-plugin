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

import ch.hsr.ifs.mockator.plugin.base.misc.CastHelper;

@SuppressWarnings("restriction")
public class CtorArgumentsCopier {

  private final ICPPASTConstructorInitializer ctorInitializer;

  public CtorArgumentsCopier(final ICPPASTConstructorInitializer initializer) {
    this.ctorInitializer = initializer;
  }

  public Collection<IASTInitializerClause> getArguments() {
    return map(list(ctorInitializer.getArguments()), (clause) -> copyPreservingNameBindings(clause));
  }

  private static <T extends IASTNode> T copyPreservingNameBindings(final T node) {
    final T copy = CastHelper.unsecureCast(node.copy());
    preserveNameBinding(copy, node);
    return copy;
  }

  private static boolean isValidType(final IBinding binding) {
    return isValidType((Object) binding);
  }

  private static boolean isValidType(final Object o) {
    return !(o instanceof IProblemBinding || o instanceof ICPPUnknownBinding || o instanceof IProblemType);
  }

  private static void preserveNameBinding(final IASTNode copy, final IASTNode original) {
    if (copy instanceof IASTName) {
      final IASTName copiedName = (IASTName) copy;
      final IASTName originalName = (IASTName) original;
      final IBinding resolvedBinding = originalName.resolveBinding();

      if (isValidType(resolvedBinding)) {
        copiedName.setBinding(resolvedBinding);
      }
    }

    final IASTNode[] children = copy.getChildren();
    for (int i = 0; i < children.length; ++i) {
      preserveNameBinding(children[i], original.getChildren()[i]);
    }
  }
}
