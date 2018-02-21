package ch.hsr.ifs.mockator.plugin.refsupport.tu;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.iltis.core.resources.FileUtil;


public class SiblingTranslationUnitFinder {

   private final IFile               file;
   private final IASTTranslationUnit ast;
   private final IIndex              index;

   public SiblingTranslationUnitFinder(final IFile file, final IASTTranslationUnit ast, final IIndex index) {
      this.file = file;
      this.ast = ast;
      this.index = index;
   }

   public Optional<String> getSiblingTuPath() throws CoreException {
      if (ast.isHeaderUnit()) { return findSiblingSourceFile(); }

      return findSiblingHeaderFile();
   }

   private Optional<String> findSiblingSourceFile() throws CoreException {
      for (final IIndexInclude i : findIncludesToThisFile()) {
         if (getBaseName(i.getIncludedBy().getLocation().getFullPath()).equals(getBaseNameOfThisFile())) { return Optional.of(i.getIncludedBy()
               .getLocation().getURI().getPath()); }
      }

      return Optional.empty();
   }

   private Optional<String> findSiblingHeaderFile() throws CoreException {
      for (final IIndexInclude i : findIncludesOfThisFile()) {
         if (getBaseName(i.getFullName()).equals(getBaseNameOfThisFile())) {
            final IIndexFileLocation includesLoc = i.getIncludesLocation();

            if (includesLoc == null) {
               continue;
            }

            return Optional.of(includesLoc.getURI().getPath());
         }
      }

      return Optional.empty();
   }

   private static String getBaseName(final String filePath) {
      return FileUtil.getFilenameWithoutExtension(filePath);
   }

   private IIndexInclude[] findIncludesToThisFile() throws CoreException {
      return index.findIncludedBy(getThisFile());
   }

   private IIndexInclude[] findIncludesOfThisFile() throws CoreException {
      return index.findIncludes(getThisFile());
   }

   private String getBaseNameOfThisFile() {
      return getBaseName(file.getName().toString());
   }

   private IIndexFile getThisFile() throws CoreException {
      return index.getFile(ast.getLinkage().getLinkageID(), getIndexFileLocation(), ast.getSignificantMacros());
   }

   private IIndexFileLocation getIndexFileLocation() {
      return IndexLocationFactory.getWorkspaceIFL(file);
   }
}
