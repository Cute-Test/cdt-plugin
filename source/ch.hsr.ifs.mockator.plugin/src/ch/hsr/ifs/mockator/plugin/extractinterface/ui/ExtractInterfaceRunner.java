package ch.hsr.ifs.mockator.plugin.extractinterface.ui;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringRunner;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import ch.hsr.ifs.mockator.plugin.extractinterface.ExtractInterfaceRefactoring;
import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;


@SuppressWarnings("restriction")
class ExtractInterfaceRunner extends RefactoringRunner {

   public ExtractInterfaceRunner(ITextSelection selection, ICElement element, IShellProvider shell, ICProject cProject) {
      super(element, selection, shell, cProject);
   }

   @Override
   public void run() {
      ExtractInterfaceRefactoring refactoring = createRefactoring();
      RefactoringWizard wizard = createWizard(refactoring);
      run(wizard, refactoring, RefactoringSaveHelper.SAVE_REFACTORING);
   }

   private static RefactoringWizard createWizard(Refactoring refactoring) {
      return new ExtractInterfaceWizard(refactoring);
   }

   private ExtractInterfaceRefactoring createRefactoring() {
      ExtractInterfaceContext context = new ExtractInterfaceContext.ContextBuilder((ITranslationUnit) element, project, (ITextSelection) selection)
            .replaceAllOccurences(true).build();
      return new ExtractInterfaceRefactoring(context);
   }
}
