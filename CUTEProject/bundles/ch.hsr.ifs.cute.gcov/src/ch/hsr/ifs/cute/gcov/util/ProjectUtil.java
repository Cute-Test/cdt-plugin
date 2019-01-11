package ch.hsr.ifs.cute.gcov.util;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import ch.hsr.ifs.cute.gcov.GcovPlugin;


public final class ProjectUtil {

    private ProjectUtil() {}

    public static IConfiguration getConfiguration(IProject project) {
        IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
        IConfiguration config = info.getSelectedConfiguration();
        if (config == null) {
            config = info.getDefaultConfiguration();
        }
        return config;
    }

    public static void deleteMarkers(IFile file) {
        try {
            file.deleteMarkers(GcovPlugin.COVER_MARKER_TYPE, true, IResource.DEPTH_ZERO);
            file.deleteMarkers(GcovPlugin.UNCOVER_MARKER_TYPE, true, IResource.DEPTH_ZERO);
            file.deleteMarkers(GcovPlugin.PARTIALLY_MARKER_TYPE, true, IResource.DEPTH_ZERO);
        } catch (CoreException ce) {}
    }

    public static void deleteMarkers(IProject project) {
        if (project == null) {
            return;
        }
        try {
            project.accept(resource -> {
                if (resource instanceof IFile) {
                    deleteMarkers((IFile) resource);
                    return false;
                }
                return true;
            });
        } catch (CoreException e) {
            GcovPlugin.log(e);
        }
    }

    public static IProject getSelectedProject(ISelection selection) {
        if (!(selection instanceof IStructuredSelection)) return null;

        for (Object selected : ((IStructuredSelection) selection).toList()) {
            if (selected instanceof IProject) {
                return (IProject) selected;
            } else if (selected instanceof IAdaptable) {
                IProject proj = ((IAdaptable) selected).getAdapter(IProject.class);
                if (proj != null) {
                    return proj;
                }
            } else if (selected instanceof ICElement) {
                return ((ICElement) selected).getCProject().getProject();
            }
        }
        return null;
    }
}
