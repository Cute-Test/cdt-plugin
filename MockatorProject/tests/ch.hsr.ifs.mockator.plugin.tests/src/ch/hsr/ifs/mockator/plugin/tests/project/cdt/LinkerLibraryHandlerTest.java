package ch.hsr.ifs.mockator.plugin.tests.project.cdt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.ifs.mockator.plugin.project.cdt.options.LinkerLibraryHandler;
import ch.hsr.ifs.mockator.plugin.tests.CdtCppTestProject;


public class LinkerLibraryHandlerTest {

   private static final String  LINKER_LIBRARY = "dl";
   private CdtCppTestProject    project;
   private LinkerLibraryHandler handler;

   @Before
   public void setUp() throws CoreException {
      project = CdtCppTestProject.withOpenedProject();
      project.addCppNatures();
      project.activateManagedBuild();
      handler = new LinkerLibraryHandler(project.getProject());
   }

   @After
   public void tearDown() throws CoreException {
      project.dispose();
   }

   @Test
   public void hasLinkerLibraryAfterAdding() throws BuildException {
      assertFalse(project.hasLinkerLibrary(LINKER_LIBRARY));
      handler.addLibrary(LINKER_LIBRARY);
      assertTrue(handler.hasLibrary(LINKER_LIBRARY));
      assertTrue(project.hasLinkerLibrary(LINKER_LIBRARY));
   }
}
