package ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.base.util.StringUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.LinkedModeInfoCreater;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.CodanArguments;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.AbstractCreateTestDoubleQuickFix;

public class CreateTestDoubleSubTypeQuickFix extends AbstractCreateTestDoubleQuickFix {

  @Override
  protected MockatorRefactoring getRefactoring(ICElement cElement, ITextSelection selection,
      CodanArguments ca) {
    return new CreateTestDoubleSubTypeRefactoring(cElement, selection,
        (CreateTestDoubleSubTypeCodanArgs) ca);
  }

  @Override
  protected Maybe<LinkedModeInfoCreater> getLinkedModeCreator(ChangeEdit edit, IDocument doc,
      MockatorRefactoring refactoring) {
    String newClassName = StringUtil.capitalize(getNameOfNewClass());
    LinkedModeInfoCreater creator = new SubtypePolyTestDoubleSupport(edit, doc, newClassName);
    return maybe(creator);
  };

  @Override
  protected String getQfLabel() {
    return I18N.CreateObjectSeamQuickfix;
  }

  @Override
  protected CodanArguments getCodanArguments(IMarker marker) {
    return new CreateTestDoubleSubTypeCodanArgs(marker);
  }
}
