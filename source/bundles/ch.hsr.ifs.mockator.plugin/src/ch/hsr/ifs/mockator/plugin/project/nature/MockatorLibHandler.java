package ch.hsr.ifs.mockator.plugin.project.nature;

import static java.util.Collections.list;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.iltis.core.resources.FileUtil;

import ch.hsr.ifs.mockator.plugin.MockatorPlugin;
import ch.hsr.ifs.mockator.plugin.project.cdt.SourceFolderHandler;
import ch.hsr.ifs.mockator.plugin.project.cdt.options.IncludePathHandler;


public class MockatorLibHandler {

   private static final String MOCKATOR_TARGET_HEADER_FOLDER = "mockator";
   private static final String MOCKATOR_SRC_HEADER_FOLDER    = "headers";
   private final IProject      project;

   public MockatorLibHandler(final IProject project) {
      this.project = project;
   }

   public void addLibToProject() throws CoreException {
      if (hasLib()) { return; }

      final IFolder targetFolder = createMockatorFolder();
      copyLibFiles(targetFolder);
      addIncludePath(targetFolder);
   }

   private boolean hasLib() {
      return project.findMember(MOCKATOR_TARGET_HEADER_FOLDER) != null;
   }

   public void removeLibFromProject() throws CoreException {
      removeIncludePath();
      deleteFolder();

   }

   private void deleteFolder() throws CoreException {
      final SourceFolderHandler handler = new SourceFolderHandler(project);
      handler.deleteFolder(MOCKATOR_TARGET_HEADER_FOLDER, new NullProgressMonitor());
   }

   private void removeIncludePath() {
      final IFolder mockatorDir = project.getProject().getFolder(MOCKATOR_TARGET_HEADER_FOLDER);
      final IncludePathHandler handler = new IncludePathHandler(project);
      handler.removeInclude(mockatorDir);
   }

   private void addIncludePath(final IFolder mockatorDir) {
      final IncludePathHandler handler = new IncludePathHandler(project);
      handler.addInclude(mockatorDir);
   }

   private static void copyLibFiles(final IFolder destination) throws CoreException {
      final Collection<URL> files = getMockatorFiles(MOCKATOR_SRC_HEADER_FOLDER);
      copyFilesToFolder(destination, files);
   }

   private IFolder createMockatorFolder() throws CoreException {
      final SourceFolderHandler handler = new SourceFolderHandler(project);
      final IFolder targetFolder = handler.createFolder(MOCKATOR_TARGET_HEADER_FOLDER, new NullProgressMonitor());
      ILTISException.Unless.isTrue(targetFolder.exists(), "Mockator library target folder must be existing");
      return targetFolder;
   }

   private static Collection<URL> getMockatorFiles(final String folder) {
      return list(MockatorPlugin.getDefault().getBundle().findEntries(folder, "*.h", false));
   }

   private static void copyFilesToFolder(final IFolder destination, final Collection<URL> mockatorFiles) throws CoreException {
      for (final URL file : mockatorFiles) {
         final IFile targetFile = getTargetFile(destination, file);

         if (targetFile.exists()) {
            continue;
         }

         try {
            // create closes stream in any case -> no separate clean
            // necessary
            targetFile.create(file.openStream(), IResource.FORCE, new NullProgressMonitor());
         } catch (final IOException e) {
            throw new ILTISException("Was not able to copy Mockator header files", e).rethrowUnchecked();
         }
      }
   }

   private static IFile getTargetFile(final IFolder destination, final URL mockatorFile) {
      return destination.getFile(FileUtil.getFilename(mockatorFile.getFile()));
   }
}
