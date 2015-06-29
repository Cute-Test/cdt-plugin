package ch.hsr.ifs.mockator.plugin.refsupport.tu;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.changes.CreateFileChange;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.util.FileUtil;
import ch.hsr.ifs.mockator.plugin.base.util.ProjectUtil;

@SuppressWarnings("restriction")
public class TranslationUnitCreator {
  private final CRefactoringContext context;
  private final IProject project;

  public TranslationUnitCreator(IProject project, CRefactoringContext context) {
    this.project = project;
    this.context = context;
  }

  public IASTTranslationUnit createAndGetNewTu(IPath filePath, IProgressMonitor pm)
      throws CoreException {
    CreateFileChange fileChange = createFileChange(filePath);
    fileChange.perform(pm);
    return loadNewTu(filePath, pm);
  }

  private CreateFileChange createFileChange(IPath filePath) {
    return new CreateFileChange(filePath.lastSegment(), filePath, "", getCharset());
  }

  private String getCharset() {
    try {
      return project.getDefaultCharset();
    } catch (CoreException e) {
      throw new MockatorException(e);
    }
  }

  private IASTTranslationUnit loadNewTu(IPath filePath, IProgressMonitor pm) throws CoreException {
    IFile file = FileUtil.toIFile(filePath);
    ICProject cProject = ProjectUtil.getCProject(file.getProject());
    TranslationUnitLoader tuLoader = new TranslationUnitLoader(cProject, context, pm);
    return tuLoader.loadAst(file);
  }
}
