package ch.hsr.ifs.mockator.plugin.testdouble.creation.staticpoly;

import java.util.Optional;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.LinkedModeInfoCreater;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.CodanArguments;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.AbstractCreateTestDoubleQuickFix;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.staticpoly.cppstd.RefactoringByStdFactory;


public class CreateTestDoubleStaticPolyQuickFix extends AbstractCreateTestDoubleQuickFix {

   @Override
   protected MockatorRefactoring getRefactoring(final ICElement e, final ITextSelection sel, final CodanArguments ca) {
      return new RefactoringByStdFactory().getRefactoring(getCppStandard(), getCProject(), e, sel);
   }

   @Override
   protected Optional<LinkedModeInfoCreater> getLinkedModeCreator(final ChangeEdit edit, final IDocument document,
            final MockatorRefactoring refactoring) {
      final LinkedModeInfoCreater creator = new StaticPolyTestDoubleSupport(edit, document, getNameOfNewClass());
      return Optional.of(creator);
   }

   @Override
   protected String getQfLabel() {
      return I18N.CreateCompileSeamQuickfix;
   }
}
