package ch.hsr.ifs.mockator.plugin.preprocessor.qf;

import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.base.util.FileUtil;
import ch.hsr.ifs.mockator.plugin.project.cdt.options.IncludeFileHandler;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorQuickFix;
import ch.hsr.ifs.mockator.plugin.refsupport.tu.SiblingTranslationUnitFinder;

public abstract class TraceFunctionQuickFix extends MockatorQuickFix {

  protected boolean isTraceFunctionActive(IMarker marker) {
    IncludeFileHandler includeHandler = new IncludeFileHandler(getCProject().getProject());
    IResource siblingHeaderFile = getPathOfSiblingHeaderFile(marker);

    if (siblingHeaderFile == null)
      return true;

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

  protected IResource getPathOfSiblingHeaderFile(IMarker marker) {
    try {
      ITranslationUnit tu = getTranslationUnitViaWorkspace(marker);

      if (tu == null)
        return null;

      // If we have a template function then the marker will be in the header file
      if (tu.isHeaderUnit())
        return tu.getResource();

      for (String path : getSiblingFilePath(tu, getIndexFromMarker(marker)))
        return FileUtil.toIFile(path);
    } catch (CoreException e) {
      throw new MockatorException(e);
    }

    return null;
  }

  private Maybe<String> getSiblingFilePath(ITranslationUnit tu, IIndex index) throws CoreException {
    return new SiblingTranslationUnitFinder((IFile) tu.getResource(), tu.getAST(), index)
        .getSiblingTuPath();
  }
}
