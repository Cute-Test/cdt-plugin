package ch.hsr.ifs.mockator.plugin.refsupport.tu;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.base.util.FileUtil;

public class SiblingTranslationUnitFinder {
  private final IFile file;
  private final IASTTranslationUnit ast;
  private final IIndex index;

  public SiblingTranslationUnitFinder(IFile file, IASTTranslationUnit ast, IIndex index) {
    this.file = file;
    this.ast = ast;
    this.index = index;
  }

  public Maybe<String> getSiblingTuPath() throws CoreException {
    if (ast.isHeaderUnit())
      return findSiblingSourceFile();

    return findSiblingHeaderFile();
  }

  private Maybe<String> findSiblingSourceFile() throws CoreException {
    for (IIndexInclude i : findIncludesToThisFile()) {
      if (getBaseName(i.getIncludedBy().getLocation().getFullPath())
          .equals(getBaseNameOfThisFile()))
        return maybe(i.getIncludedBy().getLocation().getURI().getPath());
    }

    return none();
  }

  private Maybe<String> findSiblingHeaderFile() throws CoreException {
    for (IIndexInclude i : findIncludesOfThisFile()) {
      if (getBaseName(i.getFullName()).equals(getBaseNameOfThisFile())) {
        IIndexFileLocation includesLoc = i.getIncludesLocation();

        if (includesLoc == null) {
          continue;
        }

        return maybe(includesLoc.getURI().getPath());
      }
    }

    return none();
  }

  private static String getBaseName(String filePath) {
    return FileUtil.getFilenameWithoutExtension(filePath);
  }

  private IIndexInclude[] findIncludesToThisFile() throws CoreException {
    return index.findIncludedBy(getThisFile());
  }

  private IIndexInclude[] findIncludesOfThisFile() throws CoreException {
    return index.findIncludes(getThisFile());
  }

  private String getBaseNameOfThisFile() {
    return getBaseName(file.getFullPath().toString());
  }

  private IIndexFile getThisFile() throws CoreException {
    return index.getFile(ast.getLinkage().getLinkageID(), getIndexFileLocation(),
        ast.getSignificantMacros());
  }

  private IIndexFileLocation getIndexFileLocation() {
    return IndexLocationFactory.getWorkspaceIFL(file);
  }
}
