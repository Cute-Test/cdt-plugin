package ch.hsr.ifs.mockator.plugin.project.nature;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import ch.hsr.ifs.mockator.plugin.base.functional.F1V;

abstract class WithSelectedProjectAction implements IWorkbenchWindowActionDelegate {
  private IProject project;

  public void withProject(F1V<IProject> f) {
    if (project != null) {
      f.apply(project);
      project = null;
    }
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
    if ((selection instanceof TreeSelection)) {
      TreeSelection treeSel = (TreeSelection) selection;
      Object firstElement = treeSel.getFirstElement();

      if ((firstElement instanceof IProject)) {
        project = ((IProject) firstElement);
        return;
      }

      if ((firstElement instanceof ICElement)) {
        project = ((ICElement) firstElement).getCProject().getProject();
        return;
      }
    }

    project = null;
  }

  @Override
  public void dispose() {}

  @Override
  public void init(IWorkbenchWindow window) {}
}
