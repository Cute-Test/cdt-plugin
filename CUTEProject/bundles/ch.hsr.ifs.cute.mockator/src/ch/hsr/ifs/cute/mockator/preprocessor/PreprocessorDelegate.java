package ch.hsr.ifs.cute.mockator.preprocessor;

import org.eclipse.core.runtime.IPath;

import ch.hsr.ifs.iltis.core.core.resources.FileUtil;

import ch.hsr.ifs.cute.mockator.project.cdt.options.IncludeFileHandler;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorDelegate;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorRefactoringRunner;
import ch.hsr.ifs.cute.mockator.refsupport.utils.FileEditorOpener;


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
