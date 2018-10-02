package ch.hsr.ifs.cute.mockator.testdouble.qf;

import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.core.resources.IMarker;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;

import ch.hsr.ifs.iltis.cpp.core.resources.info.MarkerInfo;

import ch.hsr.ifs.cute.mockator.infos.MissingMemFunInfo;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorQfWithRefactoringSupport;


public abstract class AbstractTestDoubleQuickFix extends MockatorQfWithRefactoringSupport {

   @Override
   public Image getImage() {
      return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_PUBLIC_METHOD);
   }

   @Override
   public String getLabel() {
      return NLS.bind(getResolutionLabelHeader(), getNameOfTestDouble());
   }

   protected abstract String getResolutionLabelHeader();

   private String getNameOfTestDouble() {
      return getMarkerInfo(marker).testDoubleName;
   }

   @Override
   protected MissingMemFunInfo getMarkerInfo(final IMarker marker) {
      return MarkerInfo.fromCodanProblemMarker(MissingMemFunInfo::new, marker);
   }
}
