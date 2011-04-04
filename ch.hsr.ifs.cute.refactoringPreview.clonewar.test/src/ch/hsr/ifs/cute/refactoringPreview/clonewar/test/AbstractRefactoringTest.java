package ch.hsr.ifs.cute.refactoringPreview.clonewar.test;

import java.util.Properties;
import java.util.Vector;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.TestSourceFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.CloneWarRefactoring;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.test.condition.ConditionCheckStrategy;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.test.configuration.NullTestConfigurationStrategy;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.test.configuration.TestConfigurationStrategy;

/**
 * Helper class providing the base refactoringtest-lifecycle. A refactoring
 * test always performs the same steps, which are combined in the test method
 * of this base class, rather than directly derive from {@link RefactoringTest}
 * and implement the same workflow twice.
 * @author ythrier(at)hsr.ch
 *
 */
@SuppressWarnings("restriction")
public class AbstractRefactoringTest extends RefactoringTest {
	private TestConfigurationStrategy testStrategy_ = new NullTestConfigurationStrategy();
	private ConditionCheckStrategy initialCheckStrategy_;
	private ConditionCheckStrategy finalCheckStrategy_;
	private CRefactoring refactoring_;
	private Class<?> exception_;
	
	/**
	 * See {@link RefactoringTest}.
	 * @param name Name.
	 * @param files Files.
	 */
	public AbstractRefactoringTest(String name, Vector<TestSourceFile> files) {
		super(name, files);
	}
	
	/**
	 * Set the initial check strategy.
	 * @param initialCheckStrategy Initial check strategy.
	 */
	public void setInitialCheckStrategy(ConditionCheckStrategy initialCheckStrategy) {
		this.initialCheckStrategy_ = initialCheckStrategy;
	}

	/**
	 * Set the final check strategy.
	 * @param finalCheckStrategy Final check strategy.
	 */
	public void setFinalCheckStrategy(ConditionCheckStrategy finalCheckStrategy) {
		this.finalCheckStrategy_ = finalCheckStrategy;
	}
	
	/**
	 * Set the test strategy to adjust the configuration of the refactoring.
	 * @param testStrategy Test strategy.
	 */
	public void setTestStrategy(TestConfigurationStrategy testStrategy) {
		this.testStrategy_ = testStrategy;
	}
	
	/**
	 * Set the expected exception for the refactoring.
	 * @param expectedException Expected exception.
	 */
	public void setExceptionClass(Class<?> expectedException) {
		this.exception_ = expectedException;
	}

	/**
	 * Returns the refactoring.
	 * @return Refactoring.
	 */
	protected CloneWarRefactoring getRefactoring() {
		return (CloneWarRefactoring) refactoring_;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runTest() throws Throwable {
		refactoring_ = createRefactoring();
		try {
			refactoring_.lockIndex();
			if(!initialCheckStrategy_.checkCondition(this, performInitCheck())) {
				compareFiles(fileMap);				
				return;
			}
			testStrategy_.changeConfiguration(getRefactoring());
			if(!finalCheckStrategy_.checkCondition(this, performFinalCheck())) {
				compareFiles(fileMap);
				return;
			}
			Change change = refactoring_.createChange(NULL_PROGRESS_MONITOR);
			change.perform(NULL_PROGRESS_MONITOR);
		} catch(Throwable e) {
			if(exception_==null || !exception_.equals(e.getClass()))
				throw e;
			compareFiles(fileMap);
			return;
		} finally {
			refactoring_.unlockIndex();
		}
		if(exception_!=null)
			fail();
		compareFiles(fileMap);
	}

	/**
	 * Invokes {@link CRefactoring#checkFinalConditions(org.eclipse.core.runtime.IProgressMonitor)}.
	 * Override to add custom behavior.
	 * @return Status of final method.
	 * @throws CoreException See {@link CoreException}.
	 * @throws OperationCanceledException See {@link OperationCanceledException}.
	 */
	protected RefactoringStatus performFinalCheck() throws OperationCanceledException, CoreException {
		return refactoring_.checkFinalConditions(NULL_PROGRESS_MONITOR);
	}

	/**
	 * Invokes {@link CRefactoring#checkInitialConditions(org.eclipse.core.runtime.IProgressMonitor)}.
	 * Override to add custom behavior.
	 * @return Status of init method.
	 * @throws CoreException See {@link CoreException}.
	 * @throws OperationCanceledException See {@link OperationCanceledException}.
	 */
	protected RefactoringStatus performInitCheck() throws OperationCanceledException, CoreException {
		return refactoring_.checkInitialConditions(NULL_PROGRESS_MONITOR);
	}

	/**
	 * Factory method creating the refactoring to test.
	 * @return Refactoring class.
	 */
	protected CRefactoring createRefactoring() {
		return new CloneWarRefactoring(project.getFile(fileName), selection, null, null);
	}

	/**
	 * Perform assert conditions ok.
	 * @param status Refactoring status.
	 */
	public void performAssertConditionsOk(RefactoringStatus status) {
		assertConditionsOk(status);
	}
	
	/**
	 * Perform assert conditions fatal error.
	 * @param status Refactoring status.
	 */
	public void performAssertConditionsFatalError(RefactoringStatus status) {
		assertConditionsFatalError(status);
	}
	
	/**
	 * Perform assert conditions error.
	 * @param status Refactoring status.
	 */
	public void performAssertConditionsError(RefactoringStatus status) {
		assertConditionsError(status, status.getEntries().length);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configureRefactoring(Properties refactoringProperties) {}
}
