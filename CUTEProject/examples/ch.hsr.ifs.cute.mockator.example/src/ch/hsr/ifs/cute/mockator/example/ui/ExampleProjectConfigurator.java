package ch.hsr.ifs.cute.mockator.example.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.BasicMonitor;

import ch.hsr.ifs.iltis.cpp.versionator.definition.CPPVersion;

import ch.hsr.ifs.cute.headers.ICuteHeaders;
import ch.hsr.ifs.cute.mockator.project.nature.MockatorNature;
import ch.hsr.ifs.cute.ui.project.CuteNature;


public class ExampleProjectConfigurator extends ToolChainProjectConfigurator {

	@Override
	public void configureProject(IProject project, IProgressMonitor monitor) {
		super.configureProject(project, monitor);

		try {
			if (project.exists() && project.isOpen()) {
				if (!project.hasNature(MockatorNature.NATURE_ID)) {
					MockatorNature.addMockatorNature(project, BasicMonitor.subProgress(monitor, 1));
				}
				if (!project.hasNature(CuteNature.NATURE_ID)) {
                    CuteNature.addCuteNature(project, ICuteHeaders.getDefaultHeaders(CPPVersion.getForProject(project)), BasicMonitor.subProgress(monitor, 1));
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

}
