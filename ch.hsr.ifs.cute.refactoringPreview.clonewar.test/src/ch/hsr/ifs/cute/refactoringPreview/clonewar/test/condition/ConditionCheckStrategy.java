package ch.hsr.ifs.cute.refactoringPreview.clonewar.test.condition;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.cute.refactoringPreview.clonewar.test.AbstractRefactoringTest;

/**
 * Strategy to check different kind of conditions when invoking initial and final
 * condition assertion method on the refactoring test.
 * @author ythrier(at)hsr.ch
 */
public interface ConditionCheckStrategy {
	
	/**
	 * Check conditions.
	 * @param test Refactoring test.
	 * @param status Refactoring status.
	 * @return True if the refactoring test should continue, otherwise false.
	 */
	public boolean checkCondition(AbstractRefactoringTest test, RefactoringStatus status);
}
