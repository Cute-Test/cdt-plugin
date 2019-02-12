package ch.hsr.ifs.cute.mockator.project.nature;

import static ch.hsr.ifs.cute.mockator.base.util.ExceptionUtil.showException;

import java.io.IOException;

import org.eclipse.cdt.codan.core.PreferenceConstants;
import org.eclipse.cdt.codan.internal.ui.CodanUIActivator;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import ch.hsr.ifs.cute.mockator.MockatorPlugin;
import ch.hsr.ifs.cute.mockator.base.i18n.I18N;
import ch.hsr.ifs.cute.mockator.ids.IdHelper;
import ch.hsr.ifs.cute.mockator.ids.IdHelper.ProblemId;
import ch.hsr.ifs.cute.mockator.project.properties.ProjectPropertiesHandler;


@SuppressWarnings("restriction")
public class RemoveNatureAction extends WithSelectedProjectAction {

    @Override
    public void run(final IAction action) {
        removeNature();
    }

    private void removeNature() {
        withProject((p) -> {
            try {
                MockatorNature.removeMockatorNature(p, new NullProgressMonitor());
                removeCodeAnalysis(p);
            } catch (final CoreException e) {
                showException(I18N.NatureRemovalFailedTitle, I18N.NatureRemovalFailedMsg, e);
            }
        });
    }

    private void removeCodeAnalysis(IProject proj) {
        ScopedPreferenceStore preferenceStore = (ScopedPreferenceStore) CodanUIActivator.getDefault().getPreferenceStore(proj);
        ProjectPropertiesHandler propertiesHandler = new ProjectPropertiesHandler(proj);
        Boolean didChangeOverride = Boolean.valueOf(propertiesHandler.getProjectProperty(new QualifiedName(MockatorPlugin.PLUGIN_ID,
                "didChangeOverride")));
        if (didChangeOverride) {
            preferenceStore.setValue(PreferenceConstants.P_USE_PARENT, true);
        }
        for (ProblemId id : IdHelper.ProblemId.values()) {
            preferenceStore.setToDefault(id.getId());
        }
        try {
            preferenceStore.save();
        } catch (IOException e) {
            ILog logger = MockatorPlugin.getDefault().getLog();
            logger.log(new Status(IStatus.WARNING, MockatorPlugin.PLUGIN_ID, "Failed to disable mockator code analysis", e));
        }
    }

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof TreeSelection) {
            final TreeSelection treeSelection = (TreeSelection) selection;
            updateProjectFromSelection(treeSelection);
            removeNature();
        }
        return null;
    }

}
