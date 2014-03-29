package ch.hsr.ifs.mockator.plugin.refsupport.lookup;

import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.filter;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import java.net.URI;
import java.util.Collection;

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

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.functional.F1;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.base.util.ProjectUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.tu.TranslationUnitLoader;

class DefinitionFinder extends AbstractNodeFinder {

  public DefinitionFinder(ICProject projectOrigin, IIndex index, TranslationUnitLoader tuLoader) {
    super(projectOrigin, index, tuLoader);
  }

  public Maybe<IASTName> findDefinition(IASTName name) {
    return findDefinition(name.resolveBinding());
  }

  public Maybe<IASTName> findDefinition(IBinding binding) {
    try {
      return findDefinition(lookup(binding));
    } catch (CoreException e) {
      throw new MockatorException(e);
    }
  }

  public Maybe<IASTName> findDefinition(String name) {
    try {
      return findDefinition(lookup(name));
    } catch (CoreException e) {
      throw new MockatorException(e);
    }
  }

  private Maybe<IASTName> findDefinition(IIndexName[] iNames) {
    try {
      for (IIndexName iName : filterAccessibleDefinitions(iNames)) {
        for (IASTName optNode : findMatchingASTName(iName))
          // just return the first accessible definition found although
          // multiple definitions could violate C++'s one definition rule
          return maybe(optNode);
      }
    } catch (CoreException e) {
      throw new MockatorException(e);
    }

    return none();
  }

  private IIndexName[] lookup(String name) throws CoreException {
    IIndexBinding[] bind =
        index.findBindings(name.toCharArray(), IndexFilter.ALL, new NullProgressMonitor());

    if (bind.length > 0)
      return index.findDefinitions(bind[0]);

    return new IIndexName[] {};
  }

  private Collection<IIndexName> filterAccessibleDefinitions(IIndexName[] iNames) {
    return filter(iNames, new F1<IIndexName, Boolean>() {
      @Override
      public Boolean apply(IIndexName iName) {
        try {
          IIndexFile file = iName.getFile();
          URI uri = file.getLocation().getURI();
          return isPartOfOriginProject(uri) || isInOneOfReferencingPrjects(uri);
        } catch (CoreException e) {
          throw new MockatorException(e);
        }
      }
    });
  }

  private boolean isPartOfOriginProject(URI uri) {
    return ProjectUtil.isPartOfProject(uri, projectOrigin.getProject());
  }

  private boolean isInOneOfReferencingPrjects(URI uri) throws CoreException {
    for (IProject project : projectOrigin.getProject().getReferencedProjects())
      if (ProjectUtil.isPartOfProject(uri, project))
        return true;

    return false;
  }

  @Override
  protected int getLookupFlags() {
    return IIndex.FIND_DEFINITIONS;
  }
}
