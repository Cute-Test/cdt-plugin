package ch.hsr.ifs.cute.mockator.project.nature;

import static ch.hsr.ifs.cute.mockator.base.util.ExceptionUtil.showException;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.cdt.codan.core.PreferenceConstants;
import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.internal.core.CheckersRegistry;
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
public class AddNatureAction extends WithSelectedProjectAction {

    @Override
    public void run(final IAction action) {
        addNature();
    }

    private void addNature() {
        withProject((proj) -> {
            try {
                MockatorNature.addMockatorNature(proj, new NullProgressMonitor());
                enableCodeAnalysis(proj);
            } catch (final CoreException e) {
                showException(I18N.NatureAdditionFailedTitle, I18N.NatureAdditionFailedMsg, e);
            }
        });
    }

    private void enableCodeAnalysis(IProject proj) {
        ScopedPreferenceStore preferenceStore = (ScopedPreferenceStore) CodanUIActivator.getDefault().getPreferenceStore(proj);
        Boolean useProjectSettings = preferenceStore.getBoolean(PreferenceConstants.P_USE_PARENT);
        ProjectPropertiesHandler propertiesHandler = new ProjectPropertiesHandler(proj);
        propertiesHandler.setProjectProperty(new QualifiedName(MockatorPlugin.PLUGIN_ID, "didChangeOverride"), useProjectSettings.toString());
        preferenceStore.setValue(PreferenceConstants.P_USE_PARENT, false);

        List<String> mockatorProblems = Stream.of(IdHelper.ProblemId.values()).map(ProblemId::getId).collect(Collectors.toList());
        for(IChecker checker : CheckersRegistry.getInstance()) {
            Collection<IProblem> problems = CheckersRegistry.getInstance().getRefProblems(checker);
            problems.stream()
                .filter(p -> mockatorProblems.contains(p.getId()))
                .forEach(p -> {
                    preferenceStore.setValue(p.getId(), p.getSeverity().toString());
                });
        }
        

        try {
            if (preferenceStore.needsSaving()) {
                preferenceStore.save();
            }
        } catch (IOException e) {
            ILog logger = MockatorPlugin.getDefault().getLog();
            logger.log(new Status(IStatus.WARNING, MockatorPlugin.PLUGIN_ID, "Failed to enable mockator code analysis", e));
        }
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
