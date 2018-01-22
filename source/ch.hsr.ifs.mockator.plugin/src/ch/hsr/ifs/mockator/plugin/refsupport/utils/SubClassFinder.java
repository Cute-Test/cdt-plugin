package ch.hsr.ifs.mockator.plugin.refsupport.utils;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedSet;

import java.util.ArrayList;
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

   public Collection<ICPPClassType> getSubClasses(final ICPPClassType clazz) throws CoreException {
      final List<ICPPClassType> subClasses = new ArrayList<>();
      final Set<String> alreadyHandled = unorderedSet();
      collectSubClasses(clazz, subClasses, alreadyHandled);
      subClasses.remove(0);
      return subClasses;
   }

   private void collectSubClasses(final ICPPClassType clazz, final List<ICPPClassType> subClasses, final Set<String> alreadyHandled)
            throws CoreException {
      final String type = ASTTypeUtil.getType(clazz, true);

      if (!alreadyHandled.add(type)) return;

      subClasses.add(clazz);

      for (final IIndexName i : findRefsAndDefs(clazz)) {
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

   private IIndexName[] findRefsAndDefs(final ICPPBinding clazz) throws CoreException {
      return index.findNames(clazz, IIndex.FIND_REFERENCES | IIndex.FIND_DEFINITIONS);
   }
}
