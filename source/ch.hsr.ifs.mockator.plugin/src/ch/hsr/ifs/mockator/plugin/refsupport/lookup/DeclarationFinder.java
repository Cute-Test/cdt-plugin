package ch.hsr.ifs.mockator.plugin.refsupport.lookup;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;

import ch.hsr.ifs.mockator.plugin.refsupport.tu.TranslationUnitLoader;


class DeclarationFinder extends AbstractNodeFinder {

   public DeclarationFinder(ICProject projectOrigin, IIndex index, TranslationUnitLoader tuLoader) {
      super(projectOrigin, index, tuLoader);
   }

   public Collection<IASTName> findDeclarations(IASTName name) {
      return collectMatchingNames(name);
   }

   public Collection<IASTName> findDeclarations(IBinding binding) {
      return collectMatchingNames(binding);
   }

   @Override
   protected int getLookupFlags() {
      return IIndex.FIND_DECLARATIONS_DEFINITIONS;
   }
}
