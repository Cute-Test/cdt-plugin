package ch.hsr.ifs.cute.mockator.mockobject.qf;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;

import ch.hsr.ifs.iltis.cpp.core.resources.info.MarkerInfo;

import ch.hsr.ifs.cute.mockator.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.cute.mockator.mockobject.expectations.ExpectedNameCreator;
import ch.hsr.ifs.cute.mockator.mockobject.linkedmode.MockObjectLinkedEditModeFactory;
import ch.hsr.ifs.cute.mockator.project.nature.MockatorLibHandler;
import ch.hsr.ifs.cute.mockator.project.properties.AssertionOrder;
import ch.hsr.ifs.cute.mockator.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.cute.mockator.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.cute.mockator.refsupport.linkededit.LinkedModeInfoCreater;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.cute.mockator.testdouble.qf.AbstractTestDoubleQuickFix;


abstract class MockObjectQuickFix extends AbstractTestDoubleQuickFix {

   @Override
   public void apply(final IMarker marker, final IDocument document) {
      super.apply(marker, document);
      copyMockatorLibIfNecessary();
   }

   private void copyMockatorLibIfNecessary() {
      try {
         new MockatorLibHandler(getCProject().getProject()).addLibToProject();
      } catch (final CoreException e) {
         throw new ILTISException(e).rethrowUnchecked();
      }
   }

   @Override
   protected MockatorRefactoring getRefactoring(final ICElement cElement, final Optional<ITextSelection> selection, final MarkerInfo<?> info) {
      return new MockObjectRefactoring(getCppStandard(), cElement, selection, getCProject(), getLinkedEditStrategy());
   }

   protected abstract LinkedEditModeStrategy getLinkedEditStrategy();

   @Override
   protected Optional<LinkedModeInfoCreater> getLinkedModeCreator(final ChangeEdit edit, final IDocument doc, final MockatorRefactoring ref) {
      final Collection<MissingMemberFunction> missingMemFuns = getMissingMemberFunctions(ref);
      final AssertionOrder assertionOrder = AssertionOrder.fromProjectSettings(getCProject().getProject());
      final MockObjectLinkedEditModeFactory factory = new MockObjectLinkedEditModeFactory(edit, missingMemFuns, getCppStandard(), assertionOrder,
            Optional.of(getNameForExpectationsVector()));
      return Optional.of(factory.getLinkedModeInfoCreator(getLinkedEditStrategy()));
   }

   private String getNameForExpectationsVector() {
      final String testDoubleName = getMarkerInfo(marker).testDoubleName;
      return new ExpectedNameCreator(testDoubleName).getNameForExpectationsVector();
   }

   private static Collection<MissingMemberFunction> getMissingMemberFunctions(final MockatorRefactoring refactoring) {
      return ((MockObjectRefactoring) refactoring).getMemberFunctionsForLinkedEdit();
   }

   @Override
   public String getDescription() {
      return getMarkerInfo(marker).missingMemFunsForMock;
   }
}
