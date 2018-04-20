package ch.hsr.ifs.mockator.plugin.tests.project.cdt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.ifs.mockator.plugin.project.cdt.SourceFolderHandler;
import ch.hsr.ifs.mockator.plugin.tests.CdtCppTestProject;


public class SourceFolderHandlerTest {

   private static final String FOLDER_NAME = "mockator";
   private CdtCppTestProject   project;
   private SourceFolderHandler folderHandler;

   @Before
   public void setUp() throws CoreException {
      project = CdtCppTestProject.withOpenedProject();
      folderHandler = new SourceFolderHandler(project.getProject());
   }

   @After
   public void tearDown() throws CoreException {
      project.dispose();
   }

   @Test
   public void createFolder() throws CoreException {
      final IFolder folder = folderHandler.createFolder(FOLDER_NAME, new NullProgressMonitor());
      assertTrue(folder.exists());
      assertEquals(FOLDER_NAME, folder.getName());
   }

   @Test
   public void deleteFolder() throws CoreException {
      final IFolder folder = folderHandler.createFolder(FOLDER_NAME, new NullProgressMonitor());
      assertTrue(folder.exists());
      folderHandler.deleteFolder(FOLDER_NAME, new NullProgressMonitor());
      assertFalse(folder.exists());
   }
}
