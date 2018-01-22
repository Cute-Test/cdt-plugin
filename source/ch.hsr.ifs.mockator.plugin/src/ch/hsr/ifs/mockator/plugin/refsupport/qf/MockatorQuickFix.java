package ch.hsr.ifs.mockator.plugin.refsupport.qf;

import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution2;

import ch.hsr.ifs.iltis.core.functional.OptHelper;
import ch.hsr.ifs.mockator.plugin.base.util.UiUtil;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;


@SuppressWarnings("restriction")
public abstract class MockatorQuickFix extends AbstractCodanCMarkerResolution implements IMarkerResolution2 {

   protected CodanArguments ca;
   protected IMarker        marker;
   protected boolean        shouldRunInCurrentThread;
   private CEditor          cEditor;

   @Override
   public boolean isApplicable(final IMarker marker) {
      ca = getCodanArguments(marker);
      this.marker = marker;
      return super.isApplicable(marker);
   }

   protected CodanArguments getCodanArguments(final IMarker marker) {
      return null;
   }

   protected ICProject getCProject() {
      return getCElement().getCProject();
   }

   protected CppStandard getCppStandard() {
      return CppStandard.fromCompilerFlags(getCProject().getProject());
   }

   protected ICElement getCElement() {
      return OptHelper.returnIfPresentElse(UiUtil.getActiveCEditor(), (editor) -> (cEditor = editor).getInputCElement(), () -> cEditor
               .getInputCElement());
   }

   public void setRunInCurrentThread(final boolean runInCurrentThread) {
      shouldRunInCurrentThread = runInCurrentThread;
   }
}
