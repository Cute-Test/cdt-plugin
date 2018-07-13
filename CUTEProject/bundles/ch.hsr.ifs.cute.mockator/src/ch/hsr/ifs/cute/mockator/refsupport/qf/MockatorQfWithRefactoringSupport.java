package ch.hsr.ifs.cute.mockator.refsupport.qf;

import java.util.Optional;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;

import ch.hsr.ifs.cute.mockator.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.cute.mockator.refsupport.linkededit.LinkedModeInfoCreater;
import ch.hsr.ifs.cute.mockator.refsupport.linkededit.LinkedModeStarter;


public abstract class MockatorQfWithRefactoringSupport extends MockatorQuickFix {

   private IDocument document;

   @Override
   public void apply(final IMarker marker, final IDocument document) {
      this.marker = marker;
      this.document = document;
      ca = getCodanArguments(marker);
      performRefactoring();
   }

   private void performRefactoring() {
      final MockatorRefactoring refactoring = getRefactoring(getCElement(), getSelection(), ca);

      if (shouldRunInCurrentThread) {
         runInCurrentThread(refactoring);
      } else {
         runInSeparateJob(refactoring);
      }
   }

   private Optional<ITextSelection> getSelection() {
      final int offset = getOffset(marker, document);
      return Optional.of(new TextSelection(document, offset, 0));
   }

   private void runInSeparateJob(final MockatorRefactoring refactoring) {
      new MockatorRefactoringRunner(refactoring).runInNewJob((changeEdit) -> startLinkedMode(refactoring, changeEdit));
   }

   private void runInCurrentThread(final MockatorRefactoring refactoring) {
      final ChangeEdit changeEdit = new MockatorRefactoringRunner(refactoring).runInCurrentThread(new NullProgressMonitor());
      startLinkedMode(refactoring, changeEdit);
   }

   private void startLinkedMode(final MockatorRefactoring refactoring, final ChangeEdit edit) {
      getLinkedModeCreator(edit, document, refactoring).ifPresent((linkedMode) -> new LinkedModeStarter().accept(linkedMode));
   }

   protected abstract MockatorRefactoring getRefactoring(ICElement cElement, Optional<ITextSelection> selection, CodanArguments ca);

   protected abstract Optional<LinkedModeInfoCreater> getLinkedModeCreator(ChangeEdit edit, IDocument document, MockatorRefactoring refactoring);
}
