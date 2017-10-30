package ch.hsr.ifs.mockator.plugin.extractinterface.ui;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.refactoring.actions.RefactoringAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;


class ExtractInterfaceAction extends RefactoringAction {

   private final ICProject cProject;

   public ExtractInterfaceAction(String label, ICProject cProject) {
      super(label);
      this.cProject = cProject;
   }

   @Override
   public void run(IShellProvider shellProvider, IWorkingCopy wc, ITextSelection selection) {
      ExtractInterfaceRunner runner = createRunner(shellProvider, wc, selection);
      runner.run();
   }

   private ExtractInterfaceRunner createRunner(IShellProvider p, IWorkingCopy wc, ITextSelection sel) {
      return new ExtractInterfaceRunner(sel, wc, p, cProject);
   }

   @Override
   public void run(IShellProvider shellProvider, ICElement elem) {}
}
