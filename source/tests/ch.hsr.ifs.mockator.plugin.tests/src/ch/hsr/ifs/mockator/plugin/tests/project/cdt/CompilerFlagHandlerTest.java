package ch.hsr.ifs.mockator.tests.project.cdt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.ifs.mockator.plugin.project.cdt.options.CompilerFlagHandler;
import ch.hsr.ifs.mockator.tests.CdtCppTestProject;


public class CompilerFlagHandlerTest {

   private static final String COMPILER_FLAG = "-ftree-loop-im";
   private CdtCppTestProject   project;
   private CompilerFlagHandler handler;

   @Before
   public void setUp() throws CoreException {
      project = CdtCppTestProject.withOpenedProject();
      project.addCppNatures();
      project.activateManagedBuild();
      handler = new CompilerFlagHandler(project.getProject());
   }

   @After
   public void tearDown() throws CoreException {
      project.dispose();
   }

   @Test
   public void hasCompilerFlagAfterAdding() throws BuildException {
      assertFalse(project.hasCompilerFlag(COMPILER_FLAG));
      handler.addCompilerFlag(COMPILER_FLAG);
      assertTrue(project.hasCompilerFlag(COMPILER_FLAG));
   }

   @Test
   public void compilerFlagMissingAfterRemoval() throws BuildException {
      handler.addCompilerFlag(COMPILER_FLAG);
      assertTrue(project.hasCompilerFlag(COMPILER_FLAG));
      handler.removeCompilerFlag(COMPILER_FLAG);
      assertFalse(project.hasCompilerFlag(COMPILER_FLAG));
   }
}
