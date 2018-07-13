package ch.hsr.ifs.cute.mockator.tests.project.cdt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.ifs.cute.mockator.tests.CdtCppTestProject;
import ch.hsr.ifs.cute.mockator.project.cdt.options.LinkerOptionHandler;


public class LinkerOptionHandlerTest {

   private static final String LINKER_OPTION = "-wrap=test";
   private CdtCppTestProject   project;
   private LinkerOptionHandler handler;

   @Before
   public void setUp() throws CoreException {
      project = CdtCppTestProject.withOpenedProject();
      project.addCppNatures();
      project.activateManagedBuild();
      handler = new LinkerOptionHandler(project.getProject());
   }

   @After
   public void tearDown() throws CoreException {
      project.dispose();
   }

   @Test
   public void hasLinkerOptionAfterAdding() throws BuildException {
      assertFalse(project.hasLinkerOption(LINKER_OPTION));
      handler.addLinkerFlag(LINKER_OPTION);
      assertTrue(project.hasLinkerOption(LINKER_OPTION));
   }

   @Test
   public void linkerOptionMissingAfterRemoval() throws BuildException {
      handler.addLinkerFlag(LINKER_OPTION);
      assertTrue(project.hasLinkerOption(LINKER_OPTION));
      handler.removeLinkerFlag(LINKER_OPTION);
      assertFalse(project.hasLinkerOption(LINKER_OPTION));
   }
}
