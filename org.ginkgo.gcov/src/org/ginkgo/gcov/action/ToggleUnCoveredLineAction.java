package org.ginkgo.gcov.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;

import ch.hsr.ifs.cute.gcov.GcovPlugin;

public class ToggleUnCoveredLineAction implements
		IWorkbenchWindowActionDelegate {
	private static final String ANNOTATION_ID = "org.ginkgo.gcov.lineUnCoverAnnotationType";
	static boolean isOn = true;
	private String hpk;
	private String tpk;
	private String ovrk;
	private String vrpk;

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		AnnotationPreferenceLookup a = EditorsUI
				.getAnnotationPreferenceLookup();
		AnnotationPreference ap = a.getAnnotationPreference(ANNOTATION_ID);
		IPreferenceStore pre = GcovPlugin.getDefault().getPreferenceStore();

		hpk = ap.getHighlightPreferenceKey();
		tpk = ap.getTextPreferenceKey();
		ovrk = ap.getOverviewRulerPreferenceKey();
		vrpk = ap.getVerticalRulerPreferenceKey();

		pre.setValue(hpk, true);
		pre.setValue(tpk, true);
		pre.setValue(ovrk, true);
		pre.setValue(vrpk, true);
		ap.setIncludeOnPreferencePage(true);

	}

	public void run(IAction action) {
		AnnotationPreferenceLookup a = EditorsUI
				.getAnnotationPreferenceLookup();
		AnnotationPreference ap = a.getAnnotationPreference(ANNOTATION_ID);
		IPreferenceStore pre = GcovPlugin.getDefault().getPreferenceStore();
		boolean hpv;
		boolean tpv;
		boolean ovrv;
		boolean vrpv;
		hpv = pre.getBoolean(hpk);
		tpv = pre.getBoolean(tpk);
		ovrv = pre.getBoolean(ovrk);
		vrpv = pre.getBoolean(vrpk);
		pre.setValue(hpk, !hpv);
		pre.setValue(tpk, !tpv);
		pre.setValue(ovrk, !ovrv);
		pre.setValue(vrpk, !vrpv);
		ap.setIncludeOnPreferencePage(isOn);
		isOn = !isOn;
	}

	public void selectionChanged(IAction action, ISelection selection) {

	}

}
