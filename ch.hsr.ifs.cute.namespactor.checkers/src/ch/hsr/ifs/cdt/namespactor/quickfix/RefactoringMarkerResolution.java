package ch.hsr.ifs.cdt.namespactor.quickfix;

import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.refactoring.actions.RefactoringAction;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IWorkbenchWindow;

@SuppressWarnings("restriction")
abstract public class RefactoringMarkerResolution extends AbstractCodanCMarkerResolution {

	@Override
	public void apply(IMarker marker, IDocument document) {

		int nodeOffset = marker.getAttribute(IMarker.CHAR_START, -1);
		int length     = marker.getAttribute(IMarker.CHAR_END, -1) - nodeOffset;
		
		IWorkbenchWindow window = CUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
		boolean isEditor        = window.getActivePage().getActivePart() instanceof CEditor;
		CEditor fEditor = (isEditor) ? (CEditor)window.getActivePage().getActivePart() : null;
		
		IWorkingCopy wc = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(fEditor.getEditorInput());
		
		RefactoringAction action = getRefactoringAction();
		action.run(fEditor.getSite(), wc, new TextSelection(nodeOffset, length));
	}

	abstract protected RefactoringAction getRefactoringAction();
}