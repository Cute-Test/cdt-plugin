package ch.hsr.ifs.mockator.plugin.refsupport.utils;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.core.runtime.CoreException;

// Inspired by ClassTypeHelper.getSubClasses method
public class SubClassFinder {
  private final IIndex index;

  public SubClassFinder(IIndex index) {
    this.index = index;
  }

  public Collection<ICPPClassType> getSubClasses(ICPPClassType klass) throws CoreException {
    List<ICPPClassType> subClasses = list();
    Set<String> alreadyHandled = unorderedSet();
    collectSubClasses(klass, subClasses, alreadyHandled);
    subClasses.remove(0);
    return subClasses;
  }

  private void collectSubClasses(ICPPClassType klass, List<ICPPClassType> subClasses,
      Set<String> alreadyHandled) throws CoreException {
    String type = ASTTypeUtil.getType(klass, true);

    if (!alreadyHandled.add(type))
      return;

    subClasses.add(klass);

    for (IIndexName i : findRefsAndDefs(klass)) {
      if (!i.isBaseSpecifier()) {
        continue;
      }

      IIndexName subClassDef = i.getEnclosingDefinition();

      if (subClassDef == null) {
        continue;
      }

      IBinding subClass = index.findBinding(subClassDef);

      if (subClass instanceof ICPPClassType) {
        collectSubClasses((ICPPClassType) subClass, subClasses, alreadyHandled);
      }
    }
  }

  private IIndexName[] findRefsAndDefs(ICPPBinding klass) throws CoreException {
    return index.findNames(klass, IIndex.FIND_REFERENCES | IIndex.FIND_DEFINITIONS);
  }
}
