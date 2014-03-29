package ch.hsr.ifs.mockator.plugin.testdouble.movetons;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;

import ch.hsr.ifs.mockator.plugin.base.util.UiUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorDelegate;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoringRunner;

public class MoveTestDoubleToNsDelegate extends MockatorDelegate {

  @Override
  protected void execute() {
    MoveTestDoubleToNsRefactoring refactoring = moveTestDoubleToNs();
    deleteInitMockatorCall(refactoring);
  }

  private MoveTestDoubleToNsRefactoring moveTestDoubleToNs() {
    MoveTestDoubleToNsRefactoring refactoring = getMoveRefactoring();
    new MockatorRefactoringRunner(refactoring).runInCurrentThread(new NullProgressMonitor());
    return refactoring;
  }

  private void deleteInitMockatorCall(MoveTestDoubleToNsRefactoring moveRefactoring) {
    RemoveInitMockatorRefactoring deleteRefactoring = getRemoveInitRefactoring(moveRefactoring);
    new MockatorRefactoringRunner(deleteRefactoring).runInCurrentThread(new NullProgressMonitor());
  }

  private MoveTestDoubleToNsRefactoring getMoveRefactoring() {
    return new MoveTestDoubleToNsRefactoring(getCppStd(), cElement, selection, cProject);
  }

  private RemoveInitMockatorRefactoring getRemoveInitRefactoring(
      MoveTestDoubleToNsRefactoring refactoring) {
    IDocument doc = UiUtil.getCurrentDocument().get();
    ICPPASTFunctionDefinition funDef = refactoring.getTestFunction();
    RemoveInitMockatorRefactoring removeRef =
        new RemoveInitMockatorRefactoring(doc, cElement, selection, cProject);
    removeRef.setTestFunction(funDef);
    return removeRef;
  }
}
