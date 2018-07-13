package ch.hsr.ifs.mockator.plugin.example.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.BasicMonitor;

import ch.hsr.ifs.iltis.core.core.ui.examples.IExampleProjectConfigurator;

import ch.hsr.ifs.cute.ui.project.CuteNature;
import ch.hsr.ifs.mockator.plugin.project.nature.MockatorNature;


public class ExampleProjectConfigurator implements IExampleProjectConfigurator {

   @Override
   public void configureProject(IProject project, IProgressMonitor monitor) {
      try {
         if (project.exists() && project.isOpen()) {
            if (!project.hasNature(MockatorNature.NATURE_ID)) MockatorNature.addMockatorNature(project, BasicMonitor.subProgress(monitor, 1));
            if (!project.hasNature(CuteNature.NATURE_ID)) CuteNature.addCuteNature(project, BasicMonitor.subProgress(monitor, 1));
         }
      } catch (CoreException e) {
         e.printStackTrace();
      }

   }

}
