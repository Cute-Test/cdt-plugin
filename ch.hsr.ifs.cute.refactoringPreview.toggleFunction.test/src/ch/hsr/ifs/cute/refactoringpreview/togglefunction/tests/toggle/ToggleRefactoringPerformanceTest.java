package ch.hsr.ifs.cute.refactoringpreview.togglefunction.tests.toggle;

/**
 * Runs common/all toggling scenarios and measures their performance.
 */
public class ToggleRefactoringPerformanceTest extends PerformanceTestPrinter {

	public void testWithIncludeStatements() throws Exception {
		runTests("IncludeStatements.rts",1); //$NON-NLS-1$
	}

	public void testWithoutIncludeStatements() throws Exception {
		runTests("NoIncludeStatements.rts",1); //$NON-NLS-1$
	}

	public void testInClassToInHeader() throws Exception {
		runTests("InClassToInHeader.rts",1); //$NON-NLS-1$
	}

	public void testInHeaderToInClass() throws Exception {
		runTests("InHeaderToInClass.rts",1); //$NON-NLS-1$
	}

	public void testAllTests() throws Exception {
		runTests("../ToggleRefactoring.rts", 1); //$NON-NLS-1$
	}
	
	public void testReferenceTest() {
		referenceTest();
	}

}
