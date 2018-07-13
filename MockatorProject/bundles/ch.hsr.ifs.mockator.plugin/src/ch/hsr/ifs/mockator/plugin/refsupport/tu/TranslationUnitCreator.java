package ch.hsr.ifs.mockator.plugin.refsupport.tu;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.changes.CreateFileChange;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;
import ch.hsr.ifs.iltis.core.core.resources.FileUtil;
import ch.hsr.ifs.iltis.cpp.core.resources.CProjectUtil;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CRefactoringContext;


@SuppressWarnings("restriction")
public class TranslationUnitCreator {

   private final CRefactoringContext context;
   private final IProject            project;

   public TranslationUnitCreator(final IProject project, final CRefactoringContext context) {
      this.project = project;
      this.context = context;
   }

   public IASTTranslationUnit createAndGetNewTu(final IPath filePath, final IProgressMonitor pm) throws CoreException {
      final CreateFileChange fileChange = createFileChange(filePath);
      fileChange.perform(pm);
      return loadNewTu(filePath, pm);
   }

   private CreateFileChange createFileChange(final IPath filePath) {
      return new CreateFileChange(filePath.lastSegment(), filePath, "", getCharset());
   }

   private String getCharset() {
      try {
         return project.getDefaultCharset();
      } catch (final CoreException e) {
         throw new ILTISException(e).rethrowUnchecked();
      }
   }

   private IASTTranslationUnit loadNewTu(final IPath filePath, final IProgressMonitor pm) throws CoreException {
      final IFile file = FileUtil.toIFile(filePath);
      final ICProject cProject = CProjectUtil.getCProject(file.getProject());
      final TranslationUnitLoader tuLoader = new TranslationUnitLoader(cProject, context, pm);
      return tuLoader.loadAst(file);
   }
}
