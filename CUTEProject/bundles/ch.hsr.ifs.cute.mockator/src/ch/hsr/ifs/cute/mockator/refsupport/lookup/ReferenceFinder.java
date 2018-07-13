package ch.hsr.ifs.cute.mockator.refsupport.lookup;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;

import ch.hsr.ifs.cute.mockator.refsupport.tu.TranslationUnitLoader;


class ReferenceFinder extends AbstractNodeFinder {

   public ReferenceFinder(final ICProject projectOrigin, final IIndex index, final TranslationUnitLoader tuLoader) {
      super(projectOrigin, index, tuLoader);
   }

   public Collection<IASTName> findReferences(final IASTName name) {
      return collectMatchingNames(name);
   }

   @Override
   protected int getLookupFlags() {
      return IIndex.FIND_REFERENCES;
   }
}
