package ch.hsr.ifs.cute.mockator.mockobject.convert;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;

import ch.hsr.ifs.cute.mockator.mockobject.MockObject;
import ch.hsr.ifs.cute.mockator.mockobject.linkedmode.MockObjectLinkedEditModeFactory;
import ch.hsr.ifs.cute.mockator.project.nature.MockatorLibHandler;
import ch.hsr.ifs.cute.mockator.project.properties.AssertionOrder;
import ch.hsr.ifs.cute.mockator.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.cute.mockator.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.cute.mockator.refsupport.linkededit.LinkedModeStarter;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorDelegate;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorRefactoringRunner;
import ch.hsr.ifs.cute.mockator.testdouble.entities.ExistingTestDoubleMemFun;


public class ConvertToMockObjectDelegate extends MockatorDelegate {

    @Override
    protected void execute() {
        copyMockatorLibIfNecessary();
        performRefactoring();
    }

    private void copyMockatorLibIfNecessary() {
        try {
            new MockatorLibHandler(cProject.getProject()).addLibToProject();
        } catch (final CoreException e) {
            throw new ILTISException(e).rethrowUnchecked();
        }
    }

    private void performRefactoring() {
        final ConvertToMockObjectRefactoring refactoring = getRefactoring();
        new MockatorRefactoringRunner(refactoring).runInNewJob((edit) -> startLinkedMode(edit, refactoring.getNewMockObject().getPublicMemFuns(),
                refactoring.getNewMockObject()));
    }

    private ConvertToMockObjectRefactoring getRefactoring() {
        return new ConvertToMockObjectRefactoring(getCppStd(), cElement, selection, cProject, getLinkedEditStrategy());
    }

    private void startLinkedMode(final ChangeEdit edit, final Collection<ExistingTestDoubleMemFun> memFuns, final MockObject mockObject) {
        final MockObjectLinkedEditModeFactory factory = new MockObjectLinkedEditModeFactory(edit, memFuns, getCppStd(), getAssertionOrder(), Optional
                .of(mockObject.getNameForExpectationVector()));
        new LinkedModeStarter().accept(factory.getLinkedModeInfoCreator(getLinkedEditStrategy()));
    }

    private AssertionOrder getAssertionOrder() {
        return AssertionOrder.fromProjectSettings(cProject.getProject());
    }

    private LinkedEditModeStrategy getLinkedEditStrategy() {
        return getAssertionOrder().getLinkedEditModeStrategy(cProject.getProject());
    }
}
