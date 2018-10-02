package ch.hsr.ifs.cute.mockator.testdouble.creation.staticpoly;

import java.util.Optional;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.iltis.cpp.core.resources.info.MarkerInfo;

import ch.hsr.ifs.cute.mockator.base.i18n.I18N;
import ch.hsr.ifs.cute.mockator.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.cute.mockator.refsupport.linkededit.LinkedModeInfoCreater;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.cute.mockator.testdouble.creation.AbstractCreateTestDoubleQuickFix;
import ch.hsr.ifs.cute.mockator.testdouble.creation.staticpoly.cppstd.RefactoringByStdFactory;


public class CreateTestDoubleStaticPolyQuickFix extends AbstractCreateTestDoubleQuickFix {

   @Override
   protected MockatorRefactoring getRefactoring(final ICElement e, final Optional<ITextSelection> sel, final MarkerInfo<?> info) {
      return new RefactoringByStdFactory().getRefactoring(e, sel, getCProject(), getCppStandard());
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
