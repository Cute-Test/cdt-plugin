package ch.hsr.ifs.cute.mockator.refsupport.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;
import ch.hsr.ifs.iltis.cpp.core.wrappers.IndexToASTNameHelper;

import ch.hsr.ifs.cute.mockator.refsupport.tu.TranslationUnitLoader;


abstract class AbstractNodeFinder {

   protected final ICProject             projectOrigin;
   protected final IIndex                index;
   protected final TranslationUnitLoader tuLoader;

   public AbstractNodeFinder(final ICProject projectOrigin, final IIndex index, final TranslationUnitLoader tuLoader) {
      this.projectOrigin = projectOrigin;
      this.index = index;
      this.tuLoader = tuLoader;
   }

   protected Collection<IASTName> collectMatchingNames(final IASTName name) {
      return collectMatchingNames(name.resolveBinding());
   }

   protected Collection<IASTName> collectMatchingNames(final IBinding name) {
      final List<IASTName> names = new ArrayList<>();
      try {
         final IIndexName[] lookup = lookup(name);
         for (final IIndexName iName : lookup) {
            findMatchingASTName(iName).ifPresent((astName) -> {
               names.add(astName);
            });
         }
      } catch (final CoreException e) {
         throw new ILTISException(e).rethrowUnchecked();
      }

      return names;
   }

   protected Optional<IASTName> findMatchingASTName(final IIndexName name) throws CoreException {
      final IASTTranslationUnit ast = loadAst(name);
      return Optional.ofNullable(findMatchingAstName(name, ast));
   }

   private IASTName findMatchingAstName(final IIndexName name, final IASTTranslationUnit ast) throws CoreException {
      return IndexToASTNameHelper.findMatchingASTName(ast, name, index);
   }

   private IASTTranslationUnit loadAst(final IIndexName name) throws CoreException {
      return tuLoader.loadAst(name);
   }

   protected IIndexName[] lookup(final IBinding binding) throws CoreException {
      final int flags = getLookupFlags() | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES;
      final IBinding adaptedBinding = index.adaptBinding(binding);
      return index.findNames(adaptedBinding, flags);
   }

   protected abstract int getLookupFlags();
}
