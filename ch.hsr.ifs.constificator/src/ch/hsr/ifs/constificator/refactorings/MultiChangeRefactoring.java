package ch.hsr.ifs.constificator.refactorings;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class MultiChangeRefactoring extends Refactoring {

	private final CompositeChange m_changes;

	public MultiChangeRefactoring(ArrayList<Change> changes) {
		m_changes = new CompositeChange("Additional const qualifications");
		m_changes.markAsSynthetic();

		for(Change change : changes) {
			if(change instanceof CompositeChange) {
				m_changes.merge((CompositeChange) change);
			} else {
				m_changes.add(change);
			}
		}
	}

	@Override
	public String getName() {
		return "Add missing const qualifications";
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return RefactoringStatus.createInfoStatus("Found missing const qualification");
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return null;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return m_changes;
	}

}
