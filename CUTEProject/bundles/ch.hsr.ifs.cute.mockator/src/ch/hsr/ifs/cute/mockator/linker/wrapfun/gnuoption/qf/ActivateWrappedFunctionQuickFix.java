package ch.hsr.ifs.cute.mockator.linker.wrapfun.gnuoption.qf;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;

import ch.hsr.ifs.cute.mockator.base.i18n.I18N;


public class ActivateWrappedFunctionQuickFix extends ToggleWrappedFunctionQuickFix {

    @Override
    public String getLabel() {
        return I18N.WrapFunctionActivate;
    }

    @Override
    public boolean isApplicable(final IMarker marker) {
        return super.isApplicable(marker) && !isWrappedFunctionActive(marker);
    }

    @Override
    public void apply(final IMarker marker, final IDocument document) {
        final String wrappedFunName = getWrappedFunName(marker);
        final WrappedFunctionQuickFixSupport support = getQfSupport();
        support.addWrapLinkerOption(wrappedFunName);
        support.addWrapMacro(wrappedFunName);
    }
}
