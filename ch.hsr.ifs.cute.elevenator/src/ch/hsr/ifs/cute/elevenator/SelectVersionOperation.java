package ch.hsr.ifs.cute.elevenator;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;

import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;
import ch.hsr.ifs.cute.elevenator.preferences.CppVersionPreferenceConstants;
import ch.hsr.ifs.cute.elevenator.view.SelectVersionWizardPage;

public class SelectVersionOperation implements IRunnableWithProgress {

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		IWizardPage[] pages = MBSCustomPageManager.getCustomPages();
		IWizard wizard = pages[0].getWizard();
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		String defaultCppVersion = store.getString(CppVersionPreferenceConstants.ELEVENATOR_VERSION_DEFAULT);

		CPPVersion selectedVersion = CPPVersion.DEFAULT;
		try {
			selectedVersion = CPPVersion.valueOf(defaultCppVersion);
		} catch (IllegalArgumentException e) {
		}

		DialectBasedSetting[] checkedModifications = null;
		// our wizard page can be anywhere, since other plug-ins can use the same extension point and add pages after
		// ours. The C++ version selection page must not be the last one in this wizard so iterate through all and do
		// not just get the last page
		for (IWizardPage page : pages) {
			if (page instanceof SelectVersionWizardPage) {
				selectedVersion = ((SelectVersionWizardPage) page).getSelectedVersion();
				checkedModifications = ((SelectVersionWizardPage) page).getCheckedModifications();
				break;
			}
		}

		if (wizard instanceof CDTCommonProjectWizard) {
			CDTCommonProjectWizard projectWizard = (CDTCommonProjectWizard) wizard;
			IProject project = projectWizard.getProject(false);

			for (Object setting : checkedModifications) {
				if (setting instanceof DialectBasedSetting) {
					// TODO: Use SafeRunner to prevent Crashing from Extensions
					((DialectBasedSetting) setting).getOperation().perform(project, selectedVersion);
				}
			}
			// EvaluateContributions.evaluateAll(project);
		}

	}
}
