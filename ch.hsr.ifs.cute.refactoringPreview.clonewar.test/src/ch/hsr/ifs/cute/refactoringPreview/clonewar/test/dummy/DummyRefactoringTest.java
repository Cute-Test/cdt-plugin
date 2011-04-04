package ch.hsr.ifs.cute.refactoringPreview.clonewar.test.dummy;

import java.util.Properties;
import java.util.Vector;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.TestSourceFile;

/**
 * Just a dummy test to get everything work.
 * @author ythrier(at)hsr.ch
 */
public class DummyRefactoringTest extends RefactoringTest {

	/**
	 * {@inheritDoc}
	 */
	public DummyRefactoringTest(String name, Vector<TestSourceFile> files) {
		super(name, files);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configureRefactoring(Properties refactoringProperties) {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runTest() throws Throwable {}
}
