package ch.hsr.ifs.mockator.plugin.extractinterface.ui;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import ch.hsr.ifs.iltis.cpp.wrappers.RefactoringRunner;

import ch.hsr.ifs.mockator.plugin.extractinterface.ExtractInterfaceRefactoring;
import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;


class ExtractInterfaceRunner extends RefactoringRunner {

   public ExtractInterfaceRunner(final ITextSelection selection, final ICElement element, final IShellProvider shell, final ICProject cProject) {
      super(element, selection, shell, cProject);
   }

   @Override
   public void run() {
      final ExtractInterfaceRefactoring refactoring = createRefactoring();
      final RefactoringWizard wizard = createWizard(refactoring);
      runWithContext(wizard, refactoring, SAVE_REFACTORING);
   }

   private static RefactoringWizard createWizard(final Refactoring refactoring) {
      return new ExtractInterfaceWizard(refactoring);
   }

   private ExtractInterfaceRefactoring createRefactoring() {
      final ExtractInterfaceContext context = new ExtractInterfaceContext.ContextBuilder((ITranslationUnit) getElement(), getProject(),
            (ITextSelection) getSelection()).replaceAllOccurences(true).build();
      return new ExtractInterfaceRefactoring(context);
   }
}
