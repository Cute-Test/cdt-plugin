package ch.hsr.ifs.cute.mockator.testdouble.creation;

import org.eclipse.cdt.codan.internal.core.model.CodanProblemMarker;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;

import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorQfWithRefactoringSupport;


@SuppressWarnings("restriction")
public abstract class AbstractCreateTestDoubleQuickFix extends MockatorQfWithRefactoringSupport {

   @Override
   public Image getImage() {
      return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_STRUCT);
   }

   @Override
   public String getLabel() {
      return NLS.bind(getQfLabel(), getNameOfNewClass());
   }

   protected abstract String getQfLabel();

   protected String getNameOfNewClass() {
      return CodanProblemMarker.getProblemArgument(marker, 0);
   }

   @Override
   public String getDescription() {
      return null; // no description necessary
   }
}
