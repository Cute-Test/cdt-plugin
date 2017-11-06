package ch.hsr.ifs.mockator.plugin.extractinterface.ui;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.refactoring.actions.RefactoringAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;


class ExtractInterfaceAction extends RefactoringAction {

   private final ICProject cProject;

   public ExtractInterfaceAction(final String label, final ICProject cProject) {
      super(label);
      this.cProject = cProject;
   }

   @Override
   public void run(final IShellProvider shellProvider, final IWorkingCopy wc, final ITextSelection selection) {
      final ExtractInterfaceRunner runner = createRunner(shellProvider, wc, selection);
      runner.run();
   }

   private ExtractInterfaceRunner createRunner(final IShellProvider p, final IWorkingCopy wc, final ITextSelection sel) {
      return new ExtractInterfaceRunner(sel, wc, p, cProject);
   }

   @Override
   public void run(final IShellProvider shellProvider, final ICElement elem) {}
}
