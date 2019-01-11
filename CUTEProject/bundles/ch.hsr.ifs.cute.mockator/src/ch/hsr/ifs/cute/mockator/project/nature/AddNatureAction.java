package ch.hsr.ifs.cute.mockator.project.nature;

import static ch.hsr.ifs.cute.mockator.base.util.ExceptionUtil.showException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.hsr.ifs.cute.mockator.base.i18n.I18N;


public class AddNatureAction extends WithSelectedProjectAction {

    @Override
    public void run(final IAction action) {
        addNature();
    }

    private void addNature() {
        withProject((proj) -> {
            try {
                MockatorNature.addMockatorNature(proj, new NullProgressMonitor());
            } catch (final CoreException e) {
                showException(I18N.NatureAdditionFailedTitle, I18N.NatureAdditionFailedMsg, e);
            }
        });
    }

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof TreeSelection) {
            final TreeSelection treeSelection = (TreeSelection) selection;
            updateProjectFromSelection(treeSelection);
            addNature();
        }

        return null;
    }
}
