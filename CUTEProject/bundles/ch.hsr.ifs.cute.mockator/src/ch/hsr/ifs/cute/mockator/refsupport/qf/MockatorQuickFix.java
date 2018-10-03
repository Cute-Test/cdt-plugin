package ch.hsr.ifs.cute.mockator.refsupport.qf;

import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution2;

import ch.hsr.ifs.iltis.cpp.core.resources.info.MarkerInfo;

import ch.hsr.ifs.cute.mockator.base.util.UiUtil;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;


@SuppressWarnings("restriction")
public abstract class MockatorQuickFix extends AbstractCodanCMarkerResolution implements IMarkerResolution2 {

    protected MarkerInfo<?> info;
    protected IMarker       marker;
    protected boolean       shouldRunInCurrentThread;
    private CEditor         cEditor;

    @Override
    public boolean isApplicable(final IMarker marker) {
        info = getMarkerInfo(marker);
        this.marker = marker;
        return super.isApplicable(marker);
    }

    protected MarkerInfo<?> getMarkerInfo(final IMarker marker) {
        return null;
    }

    protected ICProject getCProject() {
        return getCElement().getCProject();
    }

    protected CppStandard getCppStandard() {
        return CppStandard.fromCompilerFlags(getCProject().getProject());
    }

    protected ICElement getCElement() {
        return UiUtil.getActiveCEditor().map(editor -> (cEditor = editor).getInputCElement()).orElse(cEditor.getInputCElement());
    }

    public void setRunInCurrentThread(final boolean runInCurrentThread) {
        shouldRunInCurrentThread = runInCurrentThread;
    }
}
