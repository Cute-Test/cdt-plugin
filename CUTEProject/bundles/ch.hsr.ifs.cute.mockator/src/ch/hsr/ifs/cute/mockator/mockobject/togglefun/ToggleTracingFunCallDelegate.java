package ch.hsr.ifs.cute.mockator.mockobject.togglefun;

import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.list;

import java.util.Optional;

import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.iltis.core.exception.ILTISException;

import ch.hsr.ifs.cute.mockator.mockobject.linkedmode.MockObjectLinkedEditModeFactory;
import ch.hsr.ifs.cute.mockator.project.nature.MockatorLibHandler;
import ch.hsr.ifs.cute.mockator.project.properties.AssertionOrder;
import ch.hsr.ifs.cute.mockator.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.cute.mockator.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.cute.mockator.refsupport.linkededit.LinkedModeStarter;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorDelegate;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorRefactoringRunner;


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
            throw new ILTISException(e).rethrowUnchecked();
        }
    }

    private ToggleTracingFunCallRefactoring getRefactoring() {
        return new ToggleTracingFunCallRefactoring(getCppStd(), cElement, selection, getLinkedEditStrategy());
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
