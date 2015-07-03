package ch.hsr.ifs.cute.namespactor.quickfix;

import org.eclipse.cdt.ui.refactoring.actions.RefactoringAction;

import ch.hsr.ifs.cute.namespactor.ui.td2a.TD2ARefactoringAction;
import ch.hsr.ifs.cute.namespactor.ui.td2a.TD2ARefactoringActionDelegate;

public class Typedef2AliasQuickFix extends RefactoringMarkerResolution {

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return "Typedef2Alias";
	}

	@Override
	protected RefactoringAction getRefactoringAction() {
		// TODO Auto-generated method stub
//		System.out.println("typedef2aliasfix");
		return new TD2ARefactoringAction(TD2ARefactoringActionDelegate.ACTION_ID);
	}

}
