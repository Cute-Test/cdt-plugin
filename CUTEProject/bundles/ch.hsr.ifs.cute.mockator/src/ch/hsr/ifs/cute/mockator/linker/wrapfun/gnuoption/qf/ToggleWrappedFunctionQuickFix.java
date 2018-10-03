package ch.hsr.ifs.cute.mockator.linker.wrapfun.gnuoption.qf;

import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.graphics.Image;

import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorQuickFix;


abstract class ToggleWrappedFunctionQuickFix extends MockatorQuickFix {

    @Override
    public Image getImage() {
        return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_FUNCTION);
    }

    protected boolean isWrappedFunctionActive(final IMarker marker) {
        return new WrappedFunctionQuickFixSupport(getCProject().getProject()).isWrappedFunctionActive(getWrappedFunName(marker));
    }

    protected WrappedFunctionQuickFixSupport getQfSupport() {
        return new WrappedFunctionQuickFixSupport(getCProject().getProject());
    }

    protected String getWrappedFunName(final IMarker marker) {
        return getProblemArgument(marker, 0);
    }

    @Override
    public String getDescription() {
        return null;
    }
}
