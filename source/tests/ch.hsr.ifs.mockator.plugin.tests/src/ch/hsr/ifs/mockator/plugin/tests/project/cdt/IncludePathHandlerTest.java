package ch.hsr.ifs.mockator.tests.project.cdt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.ifs.mockator.plugin.project.cdt.SourceFolderHandler;
import ch.hsr.ifs.mockator.plugin.project.cdt.options.IncludePathHandler;
import ch.hsr.ifs.mockator.tests.CdtCppTestProject;


public class IncludePathHandlerTest {

   private static final String FOLDER_NAME = "mockator";
   private CdtCppTestProject   project;
   private SourceFolderHandler folderHandler;

   @Before
   public void setUp() throws CoreException {
      project = CdtCppTestProject.withOpenedProject();
      project.addCppNatures();
      project.activateManagedBuild();
      folderHandler = new SourceFolderHandler(project.getProject());
   }

   @After
   public void tearDown() throws CoreException {
      project.dispose();
   }

   @Test
   public void toggleIncludePath() throws CoreException, BuildException {
      final IFolder folder = folderHandler.createFolder(FOLDER_NAME, new NullProgressMonitor());
      final IncludePathHandler handler = new IncludePathHandler(project.getProject());
      handler.addInclude(folder);
      final String includeText = String.format("${workspace_loc:%s${ProjName}%smockator}", File.separator, File.separator);
      assertTrue(project.hasIncludeForFolder(includeText));
      assertTrue(handler.hasInclude(folder));
      handler.removeInclude(folder);
      assertFalse(project.hasIncludeForFolder(includeText));
      assertFalse(handler.hasInclude(folder));
   }
}
