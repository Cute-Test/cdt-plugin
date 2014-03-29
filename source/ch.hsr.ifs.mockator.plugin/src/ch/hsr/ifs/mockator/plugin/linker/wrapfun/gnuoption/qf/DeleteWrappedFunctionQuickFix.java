package ch.hsr.ifs.mockator.plugin.linker.wrapfun.gnuoption.qf;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.graphics.Image;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.base.util.UiUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.LinkedModeInfoCreater;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.CodanArguments;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorQfWithRefactoringSupport;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;

public class DeleteWrappedFunctionQuickFix extends MockatorQfWithRefactoringSupport {

  @Override
  public String getLabel() {
    return I18N.WrapFunctionDelete;
  }

  @Override
  protected MockatorRefactoring getRefactoring(ICElement cElement, ITextSelection sel,
      CodanArguments ca) {
    IDocument doc = UiUtil.getCurrentDocument().get();
    return new DeleteWrappedFunctionRefactoring(cElement, sel, getCProject(), doc);
  }

  @Override
  public void apply(IMarker marker, IDocument doc) {
    super.apply(marker, doc);
    WrappedFunctionQuickFixSupport support =
        new WrappedFunctionQuickFixSupport(getCProject().getProject());
    String wrappedFunName = getWrappedFunName(marker);
    support.removeWrapLinkerOption(wrappedFunName);
    support.removeWrapMacro(wrappedFunName);
  }

  private String getWrappedFunName(IMarker marker) {
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
  protected Maybe<LinkedModeInfoCreater> getLinkedModeCreator(ChangeEdit edit, IDocument doc,
      MockatorRefactoring refactoring) {
    return none();
  }
}
