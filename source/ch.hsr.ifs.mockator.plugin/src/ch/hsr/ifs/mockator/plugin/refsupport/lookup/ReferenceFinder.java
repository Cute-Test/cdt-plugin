package ch.hsr.ifs.mockator.plugin.refsupport.lookup;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;

import ch.hsr.ifs.mockator.plugin.refsupport.tu.TranslationUnitLoader;


class ReferenceFinder extends AbstractNodeFinder {

   public ReferenceFinder(ICProject projectOrigin, IIndex index, TranslationUnitLoader tuLoader) {
      super(projectOrigin, index, tuLoader);
   }

   public Collection<IASTName> findReferences(IASTName name) {
      return collectMatchingNames(name);
   }

   @Override
   protected int getLookupFlags() {
      return IIndex.FIND_REFERENCES;
   }
}
