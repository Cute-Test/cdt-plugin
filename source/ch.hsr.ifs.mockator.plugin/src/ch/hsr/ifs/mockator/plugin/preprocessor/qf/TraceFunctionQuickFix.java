package ch.hsr.ifs.mockator.plugin.preprocessor.qf;

import java.util.Optional;

import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.iltis.core.resources.FileUtil;
import ch.hsr.ifs.mockator.plugin.project.cdt.options.IncludeFileHandler;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorQuickFix;
import ch.hsr.ifs.mockator.plugin.refsupport.tu.SiblingTranslationUnitFinder;


public abstract class TraceFunctionQuickFix extends MockatorQuickFix {

   protected boolean isTraceFunctionActive(final IMarker marker) {
      final IncludeFileHandler includeHandler = new IncludeFileHandler(getCProject().getProject());
      final IResource siblingHeaderFile = getPathOfSiblingHeaderFile(marker);

      if (siblingHeaderFile == null) {
         return true;
      }

      return includeHandler.hasInclude(siblingHeaderFile);
   }

   @Override
   public String getDescription() {
      return null;
   }

   @Override
   public Image getImage() {
      return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_FUNCTION);
   }

   protected IResource getPathOfSiblingHeaderFile(final IMarker marker) {
      try {
         final ITranslationUnit tu = getTranslationUnitViaWorkspace(marker);

         if (tu == null) {
            return null;
         }

         // If we have a template function then the marker will be in the header file
         if (tu.isHeaderUnit()) {
            return tu.getResource();
         }

         final Optional<String> path = getSiblingFilePath(tu, getIndexFromMarker(marker));
         if (path.isPresent()) {
            return FileUtil.toIFile(path.get());
         }
      } catch (final CoreException e) {
         throw new ILTISException(e).rethrowUnchecked();
      }

      return null;
   }

   private Optional<String> getSiblingFilePath(final ITranslationUnit tu, final IIndex index) throws CoreException {
      return new SiblingTranslationUnitFinder((IFile) tu.getResource(), tu.getAST(), index).getSiblingTuPath();
   }
}
