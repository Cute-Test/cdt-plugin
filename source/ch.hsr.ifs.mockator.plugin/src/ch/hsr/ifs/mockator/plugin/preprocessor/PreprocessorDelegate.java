package ch.hsr.ifs.mockator.plugin.preprocessor;

import org.eclipse.core.runtime.IPath;

import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.base.util.FileUtil;
import ch.hsr.ifs.mockator.plugin.project.cdt.options.IncludeFileHandler;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;
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
    new MockatorRefactoringRunner(refactoring).runInNewJob(new F1V<ChangeEdit>() {
      @Override
      public void apply(ChangeEdit notUsed) {
        addHeaderIncludeForProject(refactoring.getNewHeaderFilePath());
        openInEditor(refactoring.getNewSourceFilePath());
      }
    });
  }

  private void addHeaderIncludeForProject(IPath headerFilePath) {
    IncludeFileHandler handler = new IncludeFileHandler(cProject.getProject());
    handler.addInclude(FileUtil.toIFile(headerFilePath));
  }

  private PreprocessorRefactoring getRefactoring() {
    return new PreprocessorRefactoring(cElement, selection, cProject);
  }

  private static void openInEditor(IPath sourceFilePath) {
    FileEditorOpener opener = new FileEditorOpener(FileUtil.toIFile(sourceFilePath));
    opener.openInEditor();
  }
}
