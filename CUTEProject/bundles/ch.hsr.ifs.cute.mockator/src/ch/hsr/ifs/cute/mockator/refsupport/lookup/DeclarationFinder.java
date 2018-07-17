package ch.hsr.ifs.cute.mockator.refsupport.lookup;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;

import ch.hsr.ifs.cute.mockator.refsupport.tu.TranslationUnitLoader;


class DeclarationFinder extends AbstractNodeFinder {

   public DeclarationFinder(final ICProject projectOrigin, final IIndex index, final TranslationUnitLoader tuLoader) {
      super(projectOrigin, index, tuLoader);
   }

   public Collection<IASTName> findDeclarations(final IASTName name) {
      return collectMatchingNames(name);
   }

   public Collection<IASTName> findDeclarations(final IBinding binding) {
      return collectMatchingNames(binding);
   }

   @Override
   protected int getLookupFlags() {
      return IIndex.FIND_DECLARATIONS_DEFINITIONS;
   }
}
