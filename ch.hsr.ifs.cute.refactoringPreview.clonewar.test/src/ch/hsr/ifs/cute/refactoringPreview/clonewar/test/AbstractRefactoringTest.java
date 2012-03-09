package ch.hsr.ifs.cute.refactoringPreview.clonewar.test;

import java.util.Properties;
import java.util.Vector;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring2;
import org.eclipse.cdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.TestSourceFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.CloneWarRefactoring;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.test.condition.ConditionCheckStrategy;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.test.configuration.NullTestConfigurationStrategy;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.test.configuration.TestConfigurationStrategy;

/**
 * Helper class providing the base refactoringtest-lifecycle. A refactoring test always performs the same steps, which are combined in the test method
 * of this base class, rather than directly derive from {@link RefactoringTest} and implement the same workflow twice.
 * 
 * @author ythrier(at)hsr.ch
 * 
 */
@SuppressWarnings("restriction")
public class AbstractRefactoringTest extends RefactoringTest {
	private TestConfigurationStrategy testStrategy = new NullTestConfigurationStrategy();
	private ConditionCheckStrategy initialCheckStrategy;
	private ConditionCheckStrategy finalCheckStrategy;
	private CloneWarRefactoring refactoring;
	private Class<?> exception_;
	private final String LINE_SEPARATOR_BEFORE_TEST = System.getProperty("line.separator");
	private final String LINE_SEPARATOR_FOR_TEST = "\n";

	/**
	 * See {@link RefactoringTest}.
	 * 
	 * @param name
	 *            Name.
	 * @param files
	 *            Files.
	 */
	public AbstractRefactoringTest(String name, Vector<TestSourceFile> files) {
		super(name, files);
	}

	@Override
	protected void setUp() throws Exception {
		System.setProperty("line.separator", LINE_SEPARATOR_FOR_TEST);
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		System.setProperty("line.separator", LINE_SEPARATOR_BEFORE_TEST);
	}

	/**
	 * Set the initial check strategy.
	 * 
	 * @param initialCheckStrategy
	 *            Initial check strategy.
	 */
	public void setInitialCheckStrategy(ConditionCheckStrategy initialCheckStrategy) {
		this.initialCheckStrategy = initialCheckStrategy;
	}

	/**
	 * Set the final check strategy.
	 * 
	 * @param finalCheckStrategy
	 *            Final check strategy.
	 */
	public void setFinalCheckStrategy(ConditionCheckStrategy finalCheckStrategy) {
		this.finalCheckStrategy = finalCheckStrategy;
	}

	/**
	 * Set the test strategy to adjust the configuration of the refactoring.
	 * 
	 * @param testStrategy
	 *            Test strategy.
	 */
	public void setTestStrategy(TestConfigurationStrategy testStrategy) {
		this.testStrategy = testStrategy;
	}

	/**
	 * Set the expected exception for the refactoring.
	 * 
	 * @param expectedException
	 *            Expected exception.
	 */
	public void setExceptionClass(Class<?> expectedException) {
		this.exception_ = expectedException;
	}

	/**
	 * Returns the refactoring.
	 * 
	 * @return Refactoring.
	 */
	protected CloneWarRefactoring getRefactoring() {
		return refactoring;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runTest() throws Throwable {
		refactoring = createRefactoring();
		try {
			astCache.getIndex().acquireReadLock();
			if (!initialCheckStrategy.checkCondition(this, performInitCheck())) {
				compareFiles(fileMap);
				return;
			}
			testStrategy.changeConfiguration(getRefactoring());
			if (!finalCheckStrategy.checkCondition(this, performFinalCheck())) {
				compareFiles(fileMap);
				return;
			}
			Change change = refactoring.createChange(NULL_PROGRESS_MONITOR);
			change.perform(NULL_PROGRESS_MONITOR);
		} catch (Throwable e) {
			if (exception_ == null || !exception_.equals(e.getClass()))
				throw e;
			compareFiles(fileMap);
			return;
		} finally {
			astCache.getIndex().releaseReadLock();
		}
		if (exception_ != null)
			fail();
		compareFiles(fileMap);
	}

	/**
	 * Invokes {@link CRefactoring2#checkFinalConditions(org.eclipse.core.runtime.IProgressMonitor)}. Override to add custom behavior.
	 * 
	 * @return Status of final method.
	 * @throws CoreException
	 *             See {@link CoreException}.
	 * @throws OperationCanceledException
	 *             See {@link OperationCanceledException}.
	 */
	protected RefactoringStatus performFinalCheck() throws OperationCanceledException, CoreException {
		return refactoring.checkFinalConditions(NULL_PROGRESS_MONITOR);
	}

	/**
	 * Invokes {@link CRefactoring2#checkInitialConditions(org.eclipse.core.runtime.IProgressMonitor)}. Override to add custom behavior.
	 * 
	 * @return Status of init method.
	 * @throws CoreException
	 *             See {@link CoreException}.
	 * @throws OperationCanceledException
	 *             See {@link OperationCanceledException}.
	 */
	protected RefactoringStatus performInitCheck() throws OperationCanceledException, CoreException {
		return refactoring.checkInitialConditions(NULL_PROGRESS_MONITOR);
	}

	/**
	 * Factory method creating the refactoring to test.
	 * 
	 * @return Refactoring class.
	 * @throws CModelException
	 */
	protected CloneWarRefactoring createRefactoring() throws CModelException {
		return new CloneWarRefactoring(selection, cproject.findElement(new Path(fileName)), cproject, astCache);
	}

	/**
	 * Perform assert conditions ok.
	 * 
	 * @param status
	 *            Refactoring status.
	 */
	public void performAssertConditionsOk(RefactoringStatus status) {
		assertConditionsOk(status);
	}

	/**
	 * Perform assert conditions fatal error.
	 * 
	 * @param status
	 *            Refactoring status.
	 */
	public void performAssertConditionsFatalError(RefactoringStatus status) {
		assertConditionsFatalError(status);
	}

	/**
	 * Perform assert conditions error.
	 * 
	 * @param status
	 *            Refactoring status.
	 */
	public void performAssertConditionsError(RefactoringStatus status) {
		assertConditionsError(status, status.getEntries().length);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configureRefactoring(Properties refactoringProperties) {
	}
}
