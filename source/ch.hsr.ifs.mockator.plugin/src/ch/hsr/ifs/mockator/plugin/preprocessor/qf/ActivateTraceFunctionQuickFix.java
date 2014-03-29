package ch.hsr.ifs.mockator.plugin.preprocessor.qf;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.project.cdt.options.IncludeFileHandler;

public class ActivateTraceFunctionQuickFix extends TraceFunctionQuickFix {

  @Override
  public String getLabel() {
    return I18N.TraceFunctionActivate;
  }

  @Override
  public boolean isApplicable(IMarker marker) {
    return super.isApplicable(marker) && !isTraceFunctionActive(marker);
  }

  @Override
  public void apply(IMarker marker, IDocument document) {
    addIncludeFile(marker);
  }

  private void addIncludeFile(IMarker marker) {
    IncludeFileHandler handler = new IncludeFileHandler(getCProject().getProject());
    handler.addInclude(getPathOfSiblingHeaderFile(marker));
  }
}
