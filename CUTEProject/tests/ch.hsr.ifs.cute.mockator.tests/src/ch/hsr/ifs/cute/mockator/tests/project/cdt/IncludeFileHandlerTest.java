package ch.hsr.ifs.cute.mockator.tests.project.cdt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.ifs.cute.mockator.tests.CdtCppTestProject;
import ch.hsr.ifs.cute.mockator.project.cdt.options.IncludeFileHandler;


public class IncludeFileHandlerTest {

   private static final String HEADER_FILE_NAME = "mockator.h";
   private CdtCppTestProject   project;

   @Before
   public void setUp() throws CoreException {
      project = CdtCppTestProject.withOpenedProject();
      project.addCppNatures();
      project.activateManagedBuild();
   }

   @After
   public void tearDown() throws CoreException {
      project.dispose();
   }

   @Test
   public void toggleIncludeFile() throws BuildException {
      final IFile headerFile = project.getProject().getFile(HEADER_FILE_NAME);
      final IncludeFileHandler handler = new IncludeFileHandler(project.getProject());
      handler.addInclude(headerFile);
      assertTrue(handler.hasInclude(headerFile));
      final String includeText = String.format("${workspace_loc:%s${ProjName}%smockator.h}", File.separator, File.separator);
      assertTrue(project.hasIncludeForFile(includeText));
      handler.removeInclude(headerFile);
      assertFalse(handler.hasInclude(headerFile));
      assertFalse(project.hasIncludeForFile(includeText));
   }
}
