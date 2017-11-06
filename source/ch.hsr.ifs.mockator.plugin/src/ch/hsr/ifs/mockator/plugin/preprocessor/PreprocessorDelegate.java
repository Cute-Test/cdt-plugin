package ch.hsr.ifs.mockator.plugin.preprocessor;

import org.eclipse.core.runtime.IPath;

import ch.hsr.ifs.mockator.plugin.base.util.FileUtil;
import ch.hsr.ifs.mockator.plugin.project.cdt.options.IncludeFileHandler;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorDelegate;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoringRunner;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.FileEditorOpener;


public class PreprocessorDelegate extends MockatorDelegate {

   @Override
   protected void execute() {
      performRefactoring();
   }

   private void performRefactoring() {
      final PreprocessorRefactoring refactoring = getRefactoring();
      new MockatorRefactoringRunner(refactoring).runInNewJob((ignored) -> {
         addHeaderIncludeForProject(refactoring.getNewHeaderFilePath());
         openInEditor(refactoring.getNewSourceFilePath());
      });
   }

   private void addHeaderIncludeForProject(final IPath headerFilePath) {
      final IncludeFileHandler handler = new IncludeFileHandler(cProject.getProject());
      handler.addInclude(FileUtil.toIFile(headerFilePath));
   }

   private PreprocessorRefactoring getRefactoring() {
      return new PreprocessorRefactoring(cElement, selection, cProject);
   }

   private static void openInEditor(final IPath sourceFilePath) {
      final FileEditorOpener opener = new FileEditorOpener(FileUtil.toIFile(sourceFilePath));
      opener.openInEditor();
   }
}
