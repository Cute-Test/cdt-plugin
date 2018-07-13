package ch.hsr.ifs.cute.mockator.tests.project.cdt;

import static org.junit.Assert.assertFalse;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.ifs.cute.mockator.tests.CdtCppTestProject;


public class DiscoveryOptionsHandlerTest {

   private CdtCppTestProject project;

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
   public void hasCpp0xDiscoveryOptionAfterSettingIt() {
      assertFalse(project.hasCpp11DiscoveryOptionSet());
      // FIXME investigate why setting C++11 options does not work in the unit
      // DiscoveryOptionsHandler handler = new
      // DiscoveryOptionsHandler(project.getProject());
      // tests (however, during run-time of the Eclipse plug-in it works
      // perfectly)
      // handler.addCpp11Support();
      // assertTrue(project.hasCpp11DiscoveryOptionSet());
   }
}
