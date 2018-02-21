package ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype;

import java.util.Optional;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.util.StringUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.LinkedModeInfoCreater;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.CodanArguments;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.AbstractCreateTestDoubleQuickFix;


public class CreateTestDoubleSubTypeQuickFix extends AbstractCreateTestDoubleQuickFix {

   @Override
   protected MockatorRefactoring getRefactoring(final ICElement cElement, final ITextSelection selection, final CodanArguments ca) {
      return new CreateTestDoubleSubTypeRefactoring(cElement, selection, (CreateTestDoubleSubTypeCodanArgs) ca);
   }

   @Override
   protected Optional<LinkedModeInfoCreater> getLinkedModeCreator(final ChangeEdit edit, final IDocument doc, final MockatorRefactoring refactoring) {
      final String newClassName = StringUtil.capitalize(getNameOfNewClass());
      final LinkedModeInfoCreater creator = new SubtypePolyTestDoubleSupport(edit, doc, newClassName);
      return Optional.of(creator);
   };

   @Override
   protected String getQfLabel() {
      return I18N.CreateObjectSeamQuickfix;
   }

   @Override
   protected CodanArguments getCodanArguments(final IMarker marker) {
      return new CreateTestDoubleSubTypeCodanArgs(marker);
   }
}
