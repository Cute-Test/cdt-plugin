package ch.hsr.ifs.cute.mockator.linker.wrapfun.gnuoption.qf;

import java.util.Optional;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.graphics.Image;

import ch.hsr.ifs.cute.mockator.base.i18n.I18N;
import ch.hsr.ifs.cute.mockator.base.util.UiUtil;
import ch.hsr.ifs.cute.mockator.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.cute.mockator.refsupport.linkededit.LinkedModeInfoCreater;
import ch.hsr.ifs.cute.mockator.refsupport.qf.CodanArguments;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorQfWithRefactoringSupport;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorRefactoring;


public class DeleteWrappedFunctionQuickFix extends MockatorQfWithRefactoringSupport {

   @Override
   public String getLabel() {
      return I18N.WrapFunctionDelete;
   }

   @Override
   protected MockatorRefactoring getRefactoring(final ICElement cElement, final Optional<ITextSelection> sel, final CodanArguments ca) {
      final IDocument doc = UiUtil.getCurrentDocument().get();
      return new DeleteWrappedFunctionRefactoring(cElement, sel, getCProject(), doc);
   }

   @Override
   public void apply(final IMarker marker, final IDocument doc) {
      super.apply(marker, doc);
      final WrappedFunctionQuickFixSupport support = new WrappedFunctionQuickFixSupport(getCProject().getProject());
      final String wrappedFunName = getWrappedFunName(marker);
      support.removeWrapLinkerOption(wrappedFunName);
      support.removeWrapMacro(wrappedFunName);
   }

   private String getWrappedFunName(final IMarker marker) {
      return getProblemArgument(marker, 0);
   }

   @Override
   public String getDescription() {
      return getLabel();
   }

   @Override
   public Image getImage() {
      return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_FUNCTION);
   }

   @Override
   protected Optional<LinkedModeInfoCreater> getLinkedModeCreator(final ChangeEdit edit, final IDocument doc, final MockatorRefactoring refactoring) {
      return Optional.empty();
   }
}
