package ch.hsr.ifs.mockator.plugin.extractinterface.transform;

import java.util.function.Consumer;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.iltis.core.resources.FileUtil;
import ch.hsr.ifs.iltis.cpp.resources.CFileUtil;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.util.PathProposalUtil;
import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.mockator.plugin.refsupport.tu.TranslationUnitCreator;


public class InterfaceFileCreator implements Consumer<ExtractInterfaceContext> {

   @Override
   public void accept(final ExtractInterfaceContext context) {
      final IPath pathOfNewFile = getUniquePathForNewFile(context);
      context.setInterfaceFilePath(FileUtil.getFilePart(pathOfNewFile.toString()));
      createNewTu(context, pathOfNewFile);
   }

   private static IPath getUniquePathForNewFile(final ExtractInterfaceContext context) {
      final IFile classFile = CFileUtil.getFile(context.getChosenClass());
      final PathProposalUtil proposal = new PathProposalUtil(FileUtil.getPath(classFile));
      return proposal.getUniquePathForNewFile(context.getNewInterfaceName(), MockatorConstants.HEADER_SUFFIX);
   }

   private static void createNewTu(final ExtractInterfaceContext context, final IPath pathOfNewFile) {
      try {
         final TranslationUnitCreator creator = getTuCreator(context);
         final IProgressMonitor pm = context.getProgressMonitor();
         final IASTTranslationUnit newTu = creator.createAndGetNewTu(pathOfNewFile, pm);
         context.setTuOfInterface(newTu);
      }
      catch (final CoreException e) {
         new ILTISException("Not able to create new file " + pathOfNewFile.lastSegment(), e).rethrowUnchecked();;
      }
   }

   private static TranslationUnitCreator getTuCreator(final ExtractInterfaceContext context) {
      return new TranslationUnitCreator(context.getCProject().getProject(), context.getCRefContext());
   }
}
