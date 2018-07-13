package ch.hsr.ifs.mockator.plugin.linker.wrapfun.gnuoption.qf;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;


public class DeactivateWrappedFunctionQuickFix extends ToggleWrappedFunctionQuickFix {

   @Override
   public String getLabel() {
      return I18N.WrapFunctionDeactivate;
   }

   @Override
   public boolean isApplicable(final IMarker marker) {
      return super.isApplicable(marker) && isWrappedFunctionActive(marker);
   }

   @Override
   public void apply(final IMarker marker, final IDocument document) {
      final String wrappedFunName = getWrappedFunName(marker);
      final WrappedFunctionQuickFixSupport support = getQfSupport();
      support.removeWrapLinkerOption(wrappedFunName);
      support.removeWrapMacro(wrappedFunName);
   }
}
