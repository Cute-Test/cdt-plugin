package ch.hsr.ifs.mockator.plugin.mockobject.togglefun;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Optional;

import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.mockobject.linkedmode.MockObjectLinkedEditModeFactory;
import ch.hsr.ifs.mockator.plugin.project.nature.MockatorLibHandler;
import ch.hsr.ifs.mockator.plugin.project.properties.AssertionOrder;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.LinkedModeStarter;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorDelegate;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoringRunner;

public class ToggleTracingFunCallDelegate extends MockatorDelegate {

  @Override
  protected void execute() {
    copyMockatorLibIfNecessary();
    performRefactoring();
  }

  private void performRefactoring() {
    final ToggleTracingFunCallRefactoring refactoring = getRefactoring();
    new MockatorRefactoringRunner(refactoring).runInNewJob((changeEdit) -> startLinkedMode(refactoring, changeEdit));
  }

  private void copyMockatorLibIfNecessary() {
    try {
      new MockatorLibHandler(cProject.getProject()).addLibToProject();
    } catch (final CoreException e) {
      throw new MockatorException(e);
    }
  }

  private ToggleTracingFunCallRefactoring getRefactoring() {
    return new ToggleTracingFunCallRefactoring(getCppStd(), cElement, selection, cProject, getLinkedEditStrategy());
  }

  private LinkedEditModeStrategy getLinkedEditStrategy() {
    final AssertionOrder assertionOrder = getAssertionOrder();
    return assertionOrder.getLinkedEditModeStrategy(cProject.getProject());
  }

  private AssertionOrder getAssertionOrder() {
    return AssertionOrder.fromProjectSettings(cProject.getProject());
  }

  private void startLinkedMode(final ToggleTracingFunCallRefactoring refactoring, final ChangeEdit changeEdit) {
    final MockObjectLinkedEditModeFactory factory = new MockObjectLinkedEditModeFactory(changeEdit, list(refactoring.getToggledFunction()),
        getCppStd(), getAssertionOrder(), Optional.empty());
    new LinkedModeStarter().accept(factory.getLinkedModeInfoCreator(getLinkedEditStrategy()));
  }
}
