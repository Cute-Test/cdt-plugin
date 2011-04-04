package ch.hsr.ifs.cute.refactoringPreview.clonewar.test.condition;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.cute.refactoringPreview.clonewar.test.AbstractRefactoringTest;

/**
 * Assert the conditions to be ok.
 * @author ythrier(at)hsr.ch
 */
public class ConditionOkCheckStrategy implements ConditionCheckStrategy {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean checkCondition(AbstractRefactoringTest test, RefactoringStatus status) {
		test.performAssertConditionsOk(status);
		return true;
	}
}
