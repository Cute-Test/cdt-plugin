package ch.hsr.ifs.mockator.plugin.linker.wrapfun.gnuoption.qf;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;


public class ActivateWrappedFunctionQuickFix extends ToggleWrappedFunctionQuickFix {

   @Override
   public String getLabel() {
      return I18N.WrapFunctionActivate;
   }

   @Override
   public boolean isApplicable(IMarker marker) {
      return super.isApplicable(marker) && !isWrappedFunctionActive(marker);
   }

   @Override
   public void apply(IMarker marker, IDocument document) {
      String wrappedFunName = getWrappedFunName(marker);
      WrappedFunctionQuickFixSupport support = getQfSupport();
      support.addWrapLinkerOption(wrappedFunName);
      support.addWrapMacro(wrappedFunName);
   }
}
