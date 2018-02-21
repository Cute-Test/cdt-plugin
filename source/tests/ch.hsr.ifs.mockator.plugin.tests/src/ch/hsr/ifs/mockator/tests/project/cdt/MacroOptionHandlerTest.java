package ch.hsr.ifs.mockator.tests.project.cdt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.ifs.mockator.plugin.project.cdt.options.MacroOptionHandler;
import ch.hsr.ifs.mockator.tests.CdtCppTestProject;


public class MacroOptionHandlerTest {

   private static final String MACRO = "-PLAT=x86_64";
   private CdtCppTestProject   project;
   private MacroOptionHandler  handler;

   @Before
   public void setUp() throws CoreException {
      project = CdtCppTestProject.withOpenedProject();
      project.addCppNatures();
      project.activateManagedBuild();
      handler = new MacroOptionHandler(project.getProject());
   }

   @After
   public void tearDown() throws CoreException {
      project.dispose();
   }

   @Test
   public void hasMacroAfterAdding() throws BuildException {
      assertFalse(project.hasMacroSet(MACRO));
      handler.addMacro(MACRO);
      assertTrue(project.hasMacroSet(MACRO));
   }

   @Test
   public void macroMissingAfterRemoval() throws BuildException {
      handler.addMacro(MACRO);
      assertTrue(project.hasMacroSet(MACRO));
      handler.removeMacro(MACRO);
      assertFalse(project.hasMacroSet(MACRO));
   }
}
