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

   public SubClassFinder(final IIndex index) {
      this.index = index;
   }

   public Collection<ICPPClassType> getSubClasses(final ICPPClassType klass) throws CoreException {
      final List<ICPPClassType> subClasses = list();
      final Set<String> alreadyHandled = unorderedSet();
      collectSubClasses(klass, subClasses, alreadyHandled);
      subClasses.remove(0);
      return subClasses;
   }

   private void collectSubClasses(final ICPPClassType klass, final List<ICPPClassType> subClasses, final Set<String> alreadyHandled)
         throws CoreException {
      final String type = ASTTypeUtil.getType(klass, true);

      if (!alreadyHandled.add(type)) return;

      subClasses.add(klass);

      for (final IIndexName i : findRefsAndDefs(klass)) {
         if (!i.isBaseSpecifier()) {
            continue;
         }

         final IIndexName subClassDef = i.getEnclosingDefinition();

         if (subClassDef == null) {
            continue;
         }

         final IBinding subClass = index.findBinding(subClassDef);

         if (subClass instanceof ICPPClassType) {
            collectSubClasses((ICPPClassType) subClass, subClasses, alreadyHandled);
         }
      }
   }

   private IIndexName[] findRefsAndDefs(final ICPPBinding klass) throws CoreException {
      return index.findNames(klass, IIndex.FIND_REFERENCES | IIndex.FIND_DEFINITIONS);
   }
}
