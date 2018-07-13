package ch.hsr.ifs.mockator.plugin.preprocessor.qf;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.project.cdt.options.IncludeFileHandler;


public class DeactivateTraceFunctionQuickFix extends TraceFunctionQuickFix {

   @Override
   public String getLabel() {
      return I18N.TraceFunctionDeactivate;
   }

   @Override
   public boolean isApplicable(final IMarker marker) {
      return super.isApplicable(marker) && isTraceFunctionActive(marker);
   }

   @Override
   public void apply(final IMarker marker, final IDocument document) {
      removeIncludeFile(marker);
   }

   private void removeIncludeFile(final IMarker marker) {
      final IncludeFileHandler handler = new IncludeFileHandler(getCProject().getProject());
      handler.removeInclude(getPathOfSiblingHeaderFile(marker));
   }
}
