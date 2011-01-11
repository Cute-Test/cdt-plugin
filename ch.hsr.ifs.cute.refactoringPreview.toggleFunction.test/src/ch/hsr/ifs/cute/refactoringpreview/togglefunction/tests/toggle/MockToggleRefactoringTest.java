package ch.hsr.ifs.cute.refactoringpreview.togglefunction.tests.toggle;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.TextSelection;

import ch.hsr.ifs.cute.refactoringpreview.togglefunction.toggle.ToggleRefactoring;
import ch.hsr.ifs.cute.refactoringpreview.togglefunction.toggle.ToggleRefactoringContext;

public class MockToggleRefactoringTest extends ToggleRefactoring {

	public MockToggleRefactoringTest(IFile file, TextSelection selection,
			ICProject proj) {
		super(file, selection, proj);
	}

	public ToggleRefactoringContext getContext() {
		return context;
	}
}
