package ch.hsr.ifs.mockator.plugin.refsupport.qf;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;

import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.LinkedModeInfoCreater;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.LinkedModeStarter;

public abstract class MockatorQfWithRefactoringSupport extends MockatorQuickFix {
  private IDocument document;

  @Override
  public void apply(IMarker marker, IDocument document) {
    this.marker = marker;
    this.document = document;
    ca = getCodanArguments(marker);
    performRefactoring();
  }

  private void performRefactoring() {
    MockatorRefactoring refactoring = getRefactoring(getCElement(), getSelection(), ca);

    if (shouldRunInCurrentThread) {
      runInCurrentThread(refactoring);
    } else {
      runInSeparateJob(refactoring);
    }
  }

  private ITextSelection getSelection() {
    int offset = getOffset(marker, document);
    return new TextSelection(document, offset, 0);
  }

  private void runInSeparateJob(final MockatorRefactoring refactoring) {
    new MockatorRefactoringRunner(refactoring).runInNewJob(new F1V<ChangeEdit>() {
      @Override
      public void apply(ChangeEdit changeEdit) {
        startLinkedMode(refactoring, changeEdit);
      }
    });
  }

  private void runInCurrentThread(MockatorRefactoring refactoring) {
    NullProgressMonitor npm = new NullProgressMonitor();
    ChangeEdit changeEdit = new MockatorRefactoringRunner(refactoring).runInCurrentThread(npm);
    startLinkedMode(refactoring, changeEdit);
  }

  private void startLinkedMode(MockatorRefactoring refactoring, ChangeEdit edit) {
    for (LinkedModeInfoCreater optLinkedMode : getLinkedModeCreator(edit, document, refactoring)) {
      new LinkedModeStarter().apply(optLinkedMode);
    }
  }

  protected abstract MockatorRefactoring getRefactoring(ICElement cElement,
      ITextSelection selection, CodanArguments ca);

  protected abstract Maybe<LinkedModeInfoCreater> getLinkedModeCreator(ChangeEdit edit,
      IDocument document, MockatorRefactoring refactoring);
}
