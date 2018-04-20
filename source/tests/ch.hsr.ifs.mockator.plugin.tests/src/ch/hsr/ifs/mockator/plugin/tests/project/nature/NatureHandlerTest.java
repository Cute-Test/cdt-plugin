package ch.hsr.ifs.mockator.plugin.tests.project.nature;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;

import ch.hsr.ifs.mockator.plugin.project.nature.MockatorNature;
import ch.hsr.ifs.mockator.plugin.project.nature.NatureHandler;
import ch.hsr.ifs.mockator.plugin.tests.CdtCppTestProject;


public class NatureHandlerTest {

   private CdtCppTestProject project;

   @After
   public void tearDown() throws CoreException {
      project.dispose();
   }

   @Rule
   public ExpectedException thrown = ExpectedException.none();

   @Test
   public void exceptionIsThrownWithClosedProject() throws CoreException {
      thrown.expect(ILTISException.class);
      thrown.expectMessage("Only existing and open projects are supported");
      project = CdtCppTestProject.withClosedProject();
      new NatureHandler(project.getProject());
   }

   @Test
   public void exceptionIsThrownWhenNoCppNature() throws CoreException {
      thrown.expect(ILTISException.class);
      thrown.expectMessage("The set of natures is not valid.");
      project = CdtCppTestProject.withOpenedProject();
      final NatureHandler handler = new NatureHandler(project.getProject());
      handler.addNature(MockatorNature.NATURE_ID, new NullProgressMonitor());
   }

   @Test
   public void successfulWhenAddedToOpenCppProject() throws CoreException {
      project = CdtCppTestProject.withOpenedProject();
      project.addCppNatures();
      project.activateManagedBuild();
      final NatureHandler handler = new NatureHandler(project.getProject());
      handler.addNature(MockatorNature.NATURE_ID, new NullProgressMonitor());
      assertTrue(project.getProject().hasNature(MockatorNature.NATURE_ID));
   }

   @Test
   public void removeOfCppNature() throws CoreException {
      project = CdtCppTestProject.withOpenedProject();
      project.addCppNatures();
      final NatureHandler handler = new NatureHandler(project.getProject());
      handler.removeNature(CCProjectNature.CC_NATURE_ID, new NullProgressMonitor());
      assertFalse(project.getProject().hasNature(CCProjectNature.CC_NATURE_ID));
   }
}
