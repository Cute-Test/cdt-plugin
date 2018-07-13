package ch.hsr.ifs.mockator.plugin.project.nature;

import java.util.function.Consumer;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


abstract class WithSelectedProjectAction extends AbstractHandler implements IWorkbenchWindowActionDelegate {

   private IProject project;

   public void withProject(final Consumer<IProject> f) {
      if (project != null) {
         f.accept(project);
         project = null;
      }
   }

   @Override
   public void selectionChanged(final IAction action, final ISelection selection) {
      if (selection instanceof TreeSelection) {
         final TreeSelection treeSel = (TreeSelection) selection;
         updateProjectFromSelection(treeSel);
      } else {
         project = null;
      }
   }

   protected void updateProjectFromSelection(final TreeSelection treeSel) {
      final Object firstElement = treeSel.getFirstElement();

      if (firstElement instanceof IProject) {
         project = (IProject) firstElement;
         return;
      } else if (firstElement instanceof ICElement) {
         project = ((ICElement) firstElement).getCProject().getProject();
      }
   }

   @Override
   public void dispose() {}

   @Override
   public void init(final IWorkbenchWindow window) {}
}
