package ch.hsr.ifs.mockator.plugin.fakeobject;

import static ch.hsr.ifs.mockator.plugin.base.i18n.I18N.CreateMissingMemberFunctionQuickfix;

import java.util.Optional;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.LinkedModeInfoCreater;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.CodanArguments;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.mockator.plugin.testdouble.qf.AbstractTestDoubleQuickFix;


public class FakeObjectQuickFix extends AbstractTestDoubleQuickFix {

   @Override
   protected String getResolutionLabelHeader() {
      return CreateMissingMemberFunctionQuickfix;
   }

   @Override
   protected MockatorRefactoring getRefactoring(final ICElement cElement, final Optional<ITextSelection> selection, final CodanArguments ca) {
      return new FakeObjectRefactoring(getCppStandard(), cElement, selection, getCProject());
   }

   @Override
   public String getDescription() {
      return getCodanArguments(marker).getMissingMemFunsForFake();
   }

   @Override
   protected Optional<LinkedModeInfoCreater> getLinkedModeCreator(final ChangeEdit edit, final IDocument doc, final MockatorRefactoring ref) {
      return Optional.empty();
   }
}
