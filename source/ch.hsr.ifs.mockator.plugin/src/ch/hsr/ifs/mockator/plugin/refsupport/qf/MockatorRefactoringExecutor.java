package ch.hsr.ifs.mockator.plugin.refsupport.qf;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.functional.F2;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;


// Handling inspired by CDT's RefactoringJob
@SuppressWarnings("restriction")
class MockatorRefactoringExecutor implements F2<MockatorRefactoring, IProgressMonitor, ChangeEdit> {

   @Override
   public ChangeEdit apply(MockatorRefactoring refactoring, IProgressMonitor pm) {
      Change change = new NullChange();
      Change undoChange = new NullChange();
      CRefactoringContext context = new CRefactoringContext(refactoring);
      boolean success = false;

      try {
         assurePreconditions(refactoring, context, pm);
         change = createChangeObject(refactoring, context, pm);
         ChangeEdit changeEdit = new ChangeEdit(change);
         change.initializeValidationData(pm);
         assureChangeObjectIsValid(change, pm);
         RefactoringCore.getUndoManager().aboutToPerformChange(change);
         undoChange = change.perform(pm);
         success = true;
         return changeEdit;
      }
      catch (IllegalStateException e) {
         throw new MockatorException(e);
      }
      catch (CoreException e) {
         throw new MockatorException("Failure during change generation", e);
      }
      finally {
         prepareUndo(refactoring.getDescription(), change, undoChange, success, pm);
      }
   }

   private static void assureChangeObjectIsValid(Change change, IProgressMonitor pm) throws CoreException {
      if (!change.isValid(pm).isOK()) throw new MockatorException("Change object is invalid");
   }

   private static void prepareUndo(String refactoringDesc, Change change, Change undoChange, boolean success, IProgressMonitor pm) {
      undoChange.initializeValidationData(pm);
      IUndoManager undoManager = RefactoringCore.getUndoManager();
      undoManager.changePerformed(change, success);

      try {
         if (success && undoChange.isValid(pm).isOK()) {
            String name = String.format("Undo '%s'", refactoringDesc);
            undoManager.addUndo(name, undoChange);
         }
      }
      catch (CoreException e) {}
   }

   private static void assurePreconditions(MockatorRefactoring refactoring, CRefactoringContext context, IProgressMonitor pm) {
      try {
         RefactoringStatus status = refactoring.checkAllConditions(pm);

         if (status.hasFatalError()) {
            context.dispose();
            String message = status.getEntryWithHighestSeverity().getMessage();
            throw new MockatorException("Conditions not satisified for refactoring: " + message);
         }
      }
      catch (CoreException e) {
         context.dispose();
         throw new MockatorException(e);
      }
   }

   private static Change createChangeObject(MockatorRefactoring refactoring, CRefactoringContext context, IProgressMonitor pm) {
      try {
         return refactoring.createChange(pm);
      }
      catch (CoreException e) {
         throw new MockatorException("Creating change object failed: " + refactoring.getDescription(), e);
      }
      finally {
         context.dispose();
      }
   }
}
