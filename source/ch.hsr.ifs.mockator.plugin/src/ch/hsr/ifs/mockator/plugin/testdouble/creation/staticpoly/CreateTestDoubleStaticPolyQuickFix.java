package ch.hsr.ifs.mockator.plugin.testdouble.creation.staticpoly;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.LinkedModeInfoCreater;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.CodanArguments;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.AbstractCreateTestDoubleQuickFix;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.staticpoly.cppstd.RefactoringByStdFactory;

public class CreateTestDoubleStaticPolyQuickFix extends AbstractCreateTestDoubleQuickFix {

  @Override
  protected MockatorRefactoring getRefactoring(ICElement e, ITextSelection sel, CodanArguments ca) {
    return new RefactoringByStdFactory().getRefactoring(getCppStandard(), getCProject(), e, sel);
  }

  @Override
  protected Maybe<LinkedModeInfoCreater> getLinkedModeCreator(ChangeEdit edit, IDocument document,
      MockatorRefactoring refactoring) {
    LinkedModeInfoCreater creator =
        new StaticPolyTestDoubleSupport(edit, document, getNameOfNewClass());
    return maybe(creator);
  }

  @Override
  protected String getQfLabel() {
    return I18N.CreateCompileSeamQuickfix;
  }
}
