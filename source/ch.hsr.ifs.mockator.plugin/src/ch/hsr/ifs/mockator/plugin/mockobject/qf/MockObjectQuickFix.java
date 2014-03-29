package ch.hsr.ifs.mockator.plugin.mockobject.qf;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;

import java.util.Collection;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.ExpectedNameCreator;
import ch.hsr.ifs.mockator.plugin.mockobject.linkedmode.MockObjectLinkedEditModeFactory;
import ch.hsr.ifs.mockator.plugin.project.nature.MockatorLibHandler;
import ch.hsr.ifs.mockator.plugin.project.properties.AssertionOrder;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.LinkedModeInfoCreater;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.CodanArguments;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.mockator.plugin.testdouble.qf.AbstractTestDoubleQuickFix;

abstract class MockObjectQuickFix extends AbstractTestDoubleQuickFix {

  @Override
  public void apply(IMarker marker, IDocument document) {
    super.apply(marker, document);
    copyMockatorLibIfNecessary();
  }

  private void copyMockatorLibIfNecessary() {
    try {
      new MockatorLibHandler(getCProject().getProject()).addLibToProject();
    } catch (CoreException e) {
      throw new MockatorException(e);
    }
  }

  @Override
  protected MockatorRefactoring getRefactoring(ICElement cElement, ITextSelection selection,
      CodanArguments ca) {
    return new MockObjectRefactoring(getCppStandard(), cElement, selection, getCProject(),
        getLinkedEditStrategy());
  }

  protected abstract LinkedEditModeStrategy getLinkedEditStrategy();

  @Override
  protected Maybe<LinkedModeInfoCreater> getLinkedModeCreator(ChangeEdit edit, IDocument doc,
      MockatorRefactoring ref) {
    Collection<MissingMemberFunction> missingMemFuns = getMissingMemberFunctions(ref);
    AssertionOrder assertionOrder = AssertionOrder.fromProjectSettings(getCProject().getProject());
    MockObjectLinkedEditModeFactory factory =
        new MockObjectLinkedEditModeFactory(edit, missingMemFuns, getCppStandard(), assertionOrder,
            maybe(getNameForExpectationsVector()));
    return maybe(factory.getLinkedModeInfoCreator(getLinkedEditStrategy()));
  }

  private String getNameForExpectationsVector() {
    String testDoubleName = getCodanArguments(marker).getTestDoubleName();
    return new ExpectedNameCreator(testDoubleName).getNameForExpectationsVector();
  }

  private static Collection<MissingMemberFunction> getMissingMemberFunctions(
      MockatorRefactoring refactoring) {
    return ((MockObjectRefactoring) refactoring).getMemberFunctionsForLinkedEdit();
  }

  @Override
  public String getDescription() {
    return getCodanArguments(marker).getMissingMemFunsForMock();
  }
}
