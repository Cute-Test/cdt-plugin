package ch.hsr.ifs.mockator.plugin.base.util;

import java.net.URI;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

public abstract class ProjectUtil {

  public static ICProject getCProject(IFile file) {
    return CoreModel.getDefault().create(file).getCProject();
  }

  public static ICProject getCProject(IProject project) {
    return CoreModel.getDefault().getCModel().getCProject(project.getName());
  }

  public static boolean isPartOfProject(URI fileUri, IProject project) {
    return project.getLocation().isPrefixOf(new Path(fileUri.getPath()));
  }

  public static IWorkspaceRoot getWorkspaceRoot() {
    return ResourcesPlugin.getWorkspace().getRoot();
  }
}
