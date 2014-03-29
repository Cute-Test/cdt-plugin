package ch.hsr.ifs.mockator.plugin.extractinterface.transform;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.base.util.FileUtil;
import ch.hsr.ifs.mockator.plugin.base.util.PathProposalUtil;
import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.mockator.plugin.refsupport.tu.TranslationUnitCreator;

public class InterfaceFileCreator implements F1V<ExtractInterfaceContext> {

  @Override
  public void apply(ExtractInterfaceContext context) {
    IPath pathOfNewFile = getUniquePathForNewFile(context);
    context.setInterfaceFilePath(FileUtil.getFilePart(pathOfNewFile.toString()));
    createNewTu(context, pathOfNewFile);
  }

  private static IPath getUniquePathForNewFile(ExtractInterfaceContext context) {
    IFile classFile = FileUtil.getFile(context.getChosenClass());
    PathProposalUtil proposal = new PathProposalUtil(FileUtil.getPath(classFile));
    return proposal.getUniquePathForNewFile(context.getNewInterfaceName(),
        MockatorConstants.HEADER_SUFFIX);
  }

  private static void createNewTu(ExtractInterfaceContext context, IPath pathOfNewFile) {
    try {
      TranslationUnitCreator creator = getTuCreator(context);
      IProgressMonitor pm = context.getProgressMonitor();
      IASTTranslationUnit newTu = creator.createAndGetNewTu(pathOfNewFile, pm);
      context.setTuOfInterface(newTu);
    } catch (CoreException e) {
      throw new MockatorException("Not able to create new file " + pathOfNewFile.lastSegment(), e);
    }
  }

  private static TranslationUnitCreator getTuCreator(ExtractInterfaceContext context) {
    return new TranslationUnitCreator(context.getCProject().getProject(), context.getCRefContext());
  }
}
