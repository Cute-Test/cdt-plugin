package org.ginkgo.gcov.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;
import org.ginkgo.gcov.GcovPlugin;

public class ToggleCoveredLineAction implements IWorkbenchWindowActionDelegate {
	private static final String ANNOTATION_ID = "org.ginkgo.gcov.lineCoverAnnotationType";
	static boolean isOn = true;
	String hpk;
	String tpk;
	String ovrk;
	String vrpk;

	public void dispose() {
		// TODO �����������ꂽ���\�b�h�E�X�^�u

	}

	public void init(IWorkbenchWindow window) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u
		AnnotationPreferenceLookup a = EditorsUI
				.getAnnotationPreferenceLookup();
		AnnotationPreference ap = a.getAnnotationPreference(ANNOTATION_ID);
		// IPreferenceStore pre =
		// EditorsPlugin.getDefault().getPreferenceStore();
		IPreferenceStore pre = GcovPlugin.getDefault().getPreferenceStore();

		// boolean aa = !ap.getTextPreferenceValue();
		// boolean ab = !ap.getHighlightPreferenceValue();
		boolean hpv;
		boolean tpv;
		boolean ahpv;
		boolean atpv;
		hpk = ap.getHighlightPreferenceKey();
		tpk = ap.getTextPreferenceKey();
		ovrk = ap.getOverviewRulerPreferenceKey();
		vrpk = ap.getVerticalRulerPreferenceKey();

		hpv = pre.getBoolean(hpk);
		tpv = pre.getBoolean(tpk);
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
		// IPreferenceStore pre =
		// EditorsPlugin.getDefault().getPreferenceStore();
		IPreferenceStore pre = GcovPlugin.getDefault().getPreferenceStore();
		boolean hpv;
		boolean tpv;
		boolean ovrv;
		boolean vrpv;
		String hpk = ap.getHighlightPreferenceKey();
		String tpk = ap.getTextPreferenceKey();
		String ovrk = ap.getOverviewRulerPreferenceKey();
		String vrpk = ap.getVerticalRulerPreferenceKey();
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
		// TODO �����������ꂽ���\�b�h�E�X�^�u

	}

}
