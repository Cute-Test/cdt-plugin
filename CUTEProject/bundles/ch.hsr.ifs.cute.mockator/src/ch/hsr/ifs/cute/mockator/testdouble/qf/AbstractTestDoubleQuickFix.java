package ch.hsr.ifs.cute.mockator.testdouble.qf;

import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.core.resources.IMarker;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;

import ch.hsr.ifs.cute.mockator.incompleteclass.checker.MissingMemFunCodanArguments;
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
      return getCodanArguments(marker).getTestDoubleName();
   }

   @Override
   protected MissingMemFunCodanArguments getCodanArguments(final IMarker marker) {
      return new MissingMemFunCodanArguments(marker);
   }
}
