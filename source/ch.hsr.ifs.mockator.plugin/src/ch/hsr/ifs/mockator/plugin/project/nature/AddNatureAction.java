package ch.hsr.ifs.mockator.plugin.project.nature;

import static ch.hsr.ifs.mockator.plugin.base.util.ExceptionUtil.showException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;


public class AddNatureAction extends WithSelectedProjectAction {

   @Override
   public void run(final IAction action) {
      addNature();
   }

   private void addNature() {
      withProject(new F1V<IProject>() {

         @Override
         public void apply(IProject proj) {
            try {
               MockatorNature.addMockatorNature(proj, new NullProgressMonitor());
            }
            catch (CoreException e) {
               showException(I18N.NatureAdditionFailedTitle, I18N.NatureAdditionFailedMsg, e);
            }
         }
      });
   }

   @Override
   public Object execute(ExecutionEvent event) throws ExecutionException {
      ISelection selection = HandlerUtil.getCurrentSelection(event);
      if ((selection instanceof TreeSelection)) {
         TreeSelection treeSelection = (TreeSelection) selection;
         updateProjectFromSelection(treeSelection);
         addNature();
      }

      return null;
   }
}
