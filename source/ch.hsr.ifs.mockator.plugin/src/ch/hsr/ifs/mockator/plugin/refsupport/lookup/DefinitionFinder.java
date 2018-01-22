package ch.hsr.ifs.mockator.plugin.refsupport.lookup;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.iltis.core.resources.ProjectUtil;
import ch.hsr.ifs.iltis.cpp.resources.CProjectUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.tu.TranslationUnitLoader;


class DefinitionFinder extends AbstractNodeFinder {

   public DefinitionFinder(final ICProject projectOrigin, final IIndex index, final TranslationUnitLoader tuLoader) {
      super(projectOrigin, index, tuLoader);
   }

   public Optional<IASTName> findDefinition(final IASTName name) {
      return findDefinition(name.resolveBinding());
   }

   public Optional<IASTName> findDefinition(final IBinding binding) {
      try {
         return findDefinition(lookup(binding));
      } catch (final CoreException e) {
         throw new ILTISException(e).rethrowUnchecked();
      }
   }

   public Optional<IASTName> findDefinition(final String name) {
      try {
         return findDefinition(lookup(name));
      } catch (final CoreException e) {
         throw new ILTISException(e).rethrowUnchecked();
      }
   }

   private Optional<IASTName> findDefinition(final IIndexName[] iNames) {
      try {
         for (final IIndexName iName : filterAccessibleDefinitions(iNames)) {
            final Optional<IASTName> matchingASTName = findMatchingASTName(iName);
            if (matchingASTName.isPresent()) {
               // just return the first accessible definition found although
               // multiple definitions could violate C++'s one definition rule
               return Optional.of(matchingASTName.get());
            }
         }
      } catch (final CoreException e) {
         throw new ILTISException(e).rethrowUnchecked();
      }

      return Optional.empty();
   }

   private IIndexName[] lookup(final String name) throws CoreException {
      final IIndexBinding[] bind = index.findBindings(name.toCharArray(), IndexFilter.ALL, new NullProgressMonitor());

      if (bind.length > 0) {
         return index.findDefinitions(bind[0]);
      }

      return new IIndexName[] {};
   }

   private Collection<IIndexName> filterAccessibleDefinitions(final IIndexName[] iNames) {
      return Arrays.asList(iNames).stream().filter((iName) -> {
         try {
            final IIndexFile file = iName.getFile();
            final URI uri = file.getLocation().getURI();
            return isPartOfOriginProject(uri) || isInOneOfReferencingPrjects(uri);
         } catch (final CoreException e) {
            throw new ILTISException(e).rethrowUnchecked();
         }
      }).collect(Collectors.toList());
   }

   private boolean isPartOfOriginProject(final URI uri) {
      return ProjectUtil.isPartOfProject(uri, projectOrigin.getProject());
   }

   private boolean isInOneOfReferencingPrjects(final URI uri) throws CoreException {
      for (final IProject project : projectOrigin.getProject().getReferencedProjects()) {
         if (ProjectUtil.isPartOfProject(uri, project)) {
            return true;
         }
      }

      return false;
   }

   @Override
   protected int getLookupFlags() {
      return IIndex.FIND_DEFINITIONS;
   }
}
