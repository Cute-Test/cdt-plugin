package ch.hsr.ifs.cute.mockator.extractinterface.ui;

import java.util.Optional;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;

import ch.hsr.ifs.iltis.cpp.core.wrappers.RefactoringAction;


class ExtractInterfaceAction extends RefactoringAction {

   private final ICProject cProject;

   public ExtractInterfaceAction(final String label, final ICProject cProject) {
      super(label);
      this.cProject = cProject;
   }

   @Override
   public void run(final IShellProvider shellProvider, final IWorkingCopy wc, final Optional<ITextSelection> selection) {
      final ExtractInterfaceRunner runner = createRunner(shellProvider, wc, selection);
      runner.run();
   }

   private ExtractInterfaceRunner createRunner(final IShellProvider p, final IWorkingCopy wc, final Optional<ITextSelection> sel) {
      return new ExtractInterfaceRunner(sel, wc, p, cProject);
   }

   @Override
   public void run(final IShellProvider shellProvider, final ICElement elem) {}
}
