package ch.hsr.ifs.cute.namespactor.quickfix;

import org.eclipse.cdt.ui.refactoring.actions.RefactoringAction;

import ch.hsr.ifs.cute.namespactor.ui.td2a.TD2ARefactoringAction;
import ch.hsr.ifs.cute.namespactor.ui.td2a.TD2ARefactoringActionDelegate;

public class Typedef2AliasQuickFix extends RefactoringMarkerResolution {

	@Override
	public String getLabel() {
		return "Convert typedef to alias";
	}

	@Override
	protected RefactoringAction getRefactoringAction() {
		return new TD2ARefactoringAction(TD2ARefactoringActionDelegate.ACTION_ID);
	}

}
