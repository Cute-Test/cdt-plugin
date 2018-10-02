package ch.hsr.ifs.cute.mockator.fakeobject;

import static ch.hsr.ifs.cute.mockator.base.i18n.I18N.CreateMissingMemberFunctionQuickfix;

import java.util.Optional;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.iltis.cpp.core.resources.info.MarkerInfo;

import ch.hsr.ifs.cute.mockator.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.cute.mockator.refsupport.linkededit.LinkedModeInfoCreater;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.cute.mockator.testdouble.qf.AbstractTestDoubleQuickFix;


public class FakeObjectQuickFix extends AbstractTestDoubleQuickFix {

   @Override
   protected String getResolutionLabelHeader() {
      return CreateMissingMemberFunctionQuickfix;
   }

   @Override
   protected MockatorRefactoring getRefactoring(final ICElement cElement, final Optional<ITextSelection> selection, final MarkerInfo<?> info) {
      return new FakeObjectRefactoring(getCppStandard(), cElement, selection, getCProject());
   }

   @Override
   public String getDescription() {
      return getMarkerInfo(marker).missingMemFunsForFake;
   }

   @Override
   protected Optional<LinkedModeInfoCreater> getLinkedModeCreator(final ChangeEdit edit, final IDocument doc, final MockatorRefactoring ref) {
      return Optional.empty();
   }
}
