package ch.hsr.ifs.cute.gcov.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.hsr.ifs.cute.gcov.util.ProjectUtil;


public class ClearMarkersHandler extends AbstractHandler {

   @Override
   public Object execute(ExecutionEvent event) throws ExecutionException {
      ISelection selection = HandlerUtil.getCurrentSelection(event);
      IProject project = ProjectUtil.getSelectedProject(selection);
      ProjectUtil.deleteMarkers(project);
      return null;
   }

}
