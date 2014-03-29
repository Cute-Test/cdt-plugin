package ch.hsr.ifs.mockator.plugin.refsupport.lookup;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;

import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.IndexToASTNameHelper;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.refsupport.tu.TranslationUnitLoader;

@SuppressWarnings("restriction")
abstract class AbstractNodeFinder {
  protected final ICProject projectOrigin;
  protected final IIndex index;
  protected final TranslationUnitLoader tuLoader;

  public AbstractNodeFinder(ICProject projectOrigin, IIndex index, TranslationUnitLoader tuLoader) {
    this.projectOrigin = projectOrigin;
    this.index = index;
    this.tuLoader = tuLoader;
  }

  protected Collection<IASTName> collectMatchingNames(IASTName name) {
    return collectMatchingNames(name.resolveBinding());
  }

  protected Collection<IASTName> collectMatchingNames(IBinding name) {
    List<IASTName> names = list();

    try {
      for (IIndexName iName : lookup(name)) {
        for (IASTName optName : findMatchingASTName(iName)) {
          names.add(optName);
        }
      }
    } catch (CoreException e) {
      throw new MockatorException(e);
    }

    return names;
  }

  protected Maybe<IASTName> findMatchingASTName(IIndexName name) throws CoreException {
    IASTTranslationUnit ast = loadAst(name);
    return maybe(findMatchingAstName(name, ast));
  }

  private IASTName findMatchingAstName(IIndexName name, IASTTranslationUnit ast)
      throws CoreException {
    return IndexToASTNameHelper.findMatchingASTName(ast, name, index);
  }

  private IASTTranslationUnit loadAst(IIndexName name) throws CoreException {
    return tuLoader.loadAst(name);
  }

  protected IIndexName[] lookup(IBinding binding) throws CoreException {
    int flags = getLookupFlags() | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES;
    return index.findNames(binding, flags);
  }

  protected abstract int getLookupFlags();
}
