package ch.hsr.ifs.cute.mockator.example.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.iltis.core.core.ui.examples.IExampleProjectConfigurator;

import ch.hsr.ifs.iltis.cpp.core.buildconfiguration.BuildConfigurator;


public class ToolChainProjectConfigurator implements IExampleProjectConfigurator {

    @Override
    public void configureProject(IProject project, IProgressMonitor monitor) {
        BuildConfigurator buildConfigurator = new BuildConfigurator(project);
        buildConfigurator.setDefaultToolChain();
        if (buildConfigurator.isSharedLibraryProject()) {
            buildConfigurator.setArtifactExtension(BuildConfigurator.getSharedLibraryExtensionForPlatform());
        }
    }
}
