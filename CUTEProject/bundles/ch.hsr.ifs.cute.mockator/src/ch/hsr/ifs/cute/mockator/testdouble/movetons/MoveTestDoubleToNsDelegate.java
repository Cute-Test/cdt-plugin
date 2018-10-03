package ch.hsr.ifs.cute.mockator.testdouble.movetons;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;

import ch.hsr.ifs.cute.mockator.base.util.UiUtil;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorDelegate;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorRefactoringRunner;


public class MoveTestDoubleToNsDelegate extends MockatorDelegate {

    @Override
    protected void execute() {
        final MoveTestDoubleToNsRefactoring refactoring = moveTestDoubleToNs();
        deleteInitMockatorCall(refactoring);
    }

    private MoveTestDoubleToNsRefactoring moveTestDoubleToNs() {
        final MoveTestDoubleToNsRefactoring refactoring = getMoveRefactoring();
        new MockatorRefactoringRunner(refactoring).runInCurrentThread(new NullProgressMonitor());
        return refactoring;
    }

    private void deleteInitMockatorCall(final MoveTestDoubleToNsRefactoring moveRefactoring) {
        final RemoveInitMockatorRefactoring deleteRefactoring = getRemoveInitRefactoring(moveRefactoring);
        new MockatorRefactoringRunner(deleteRefactoring).runInCurrentThread(new NullProgressMonitor());
    }

    private MoveTestDoubleToNsRefactoring getMoveRefactoring() {
        return new MoveTestDoubleToNsRefactoring(getCppStd(), cElement, selection, cProject);
    }

    private RemoveInitMockatorRefactoring getRemoveInitRefactoring(final MoveTestDoubleToNsRefactoring refactoring) {
        final IDocument doc = UiUtil.getCurrentDocument().get();
        final ICPPASTFunctionDefinition funDef = refactoring.getTestFunction();
        final RemoveInitMockatorRefactoring removeRef = new RemoveInitMockatorRefactoring(doc, cElement, selection, cProject);
        removeRef.setTestFunction(funDef);
        return removeRef;
    }
}
