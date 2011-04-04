package ch.hsr.ifs.cute.refactoringPreview.clonewar.test;

import java.util.List;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.test.configuration.TestConfiguration;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.test.dummy.DummyTest;

/**
 * Merging all junit tests into a suite.
 * @author ythrier(at)hsr.ch
 */
public class AllTests {
	
	/**
	 * Return all tests.
	 * @return Test suite.
	 * @throws Exception From {@link RefactoringTester}.
	 */
	public static Test suite() throws Exception{
		TestConfiguration config = new TestConfiguration("testconfig/test.xml");
		List<Test> allTests = config.getAllTests();
		TestSuite suite = new TestSuite("CloneWar TestSuite");
		for(Test test : allTests)
			suite.addTest(test);
		//***Normal JUnit-Tests***//
		suite.addTest(createAdapter(DummyTest.class));
		//***Refactoring Tests***//
//		suite.addTest(RefactoringTester.suite("ExtractTTPFunctionRefactoringTest", "testconfig/function/ExtractTTPSimple.rts"));
//		suite.addTest(RefactoringTester.suite("ExtractTTPFunctionRefactoringTest", "testconfig/function/ExtractTTPSimpleReturn.rts"));
//		suite.addTest(RefactoringTester.suite("ExtractTTPFunctionRefactoringTest", "testconfig/function/ExtractTTPWithTypeInBody.rts"));
//		suite.addTest(RefactoringTester.suite("ExtractTTPFunctionRefactoringTest", "testconfig/function/ExtractTTPWithMultipleParamTypes.rts"));
//		suite.addTest(RefactoringTester.suite("ExtractTTPFunctionRefactoringTest", "testconfig/function/ExtractTTPSimpleMemFuncNoReturn.rts"));
//		suite.addTest(RefactoringTester.suite("ExtractTTPFunctionRefactoringTest", "testconfig/function/ExtractTTPNamedTypeParamsNoReturn.rts"));
//		suite.addTest(RefactoringTester.suite("ExtractTTPFunctionRefactoringTest", "testconfig/function/ExtractTTPNamedTypeParamsWithReturn.rts"));
//		suite.addTest(RefactoringTester.suite("ExtractTTPFunctionRefactoringTest", "testconfig/function/ExtractTTPNamedTypeParamsWithReturnAndBody.rts"));
//		suite.addTest(RefactoringTester.suite("ExtractTTPFunctionRefactoringTest", "testconfig/function/ExtractTTPSimpleWithParamSelection.rts"));
//		suite.addTest(RefactoringTester.suite("ExtractTTPFunctionRefactoringTest", "testconfig/function/ExtractTTPSimpleWithParamSelectionSecond.rts"));
//		suite.addTest(RefactoringTester.suite("ExtractTTPFunctionRefactoringTest", "testconfig/function/ExtractTTPSimpleWithReturnSelection.rts"));
//		suite.addTest(RefactoringTester.suite("ExtractTTPFunctionRefactoringTest", "testconfig/function/ExtractTTPNamedTypeParamsReturnIsFirstTemplate.rts"));
//		suite.addTest(RefactoringTester.suite("ExtractTTPFunctionRefactoringTemplateTest", "testconfig/function/ExtractTTPContainerParameterType.rts"));
//		suite.addTest(RefactoringTester.suite("ExtractTTPFunctionRefactoringTemplateTest", "testconfig/function/ExtractTTPContainerReturnType.rts"));
//		suite.addTest(RefactoringTester.suite("ExtractTTPFunctionRefactoringTemplateTest", "testconfig/function/ExtractTTPContainerBodyType.rts"));
//		suite.addTest(RefactoringTester.suite("ExtractTTPFunctionRefactoringTemplateTest", "testconfig/function/ExtractTTPContainerParameterTypeNamed.rts"));
//		suite.addTest(RefactoringTester.suite("ExtractTTPFunctionRefactoringTemplateTest", "testconfig/function/ExtractTTPContainerReturnTypeNamed.rts"));
//		suite.addTest(RefactoringTester.suite("ExtractTTPFunctionRefactoringTemplateTest", "testconfig/function/ExtractTTPContainerBodyTypeNamed.rts"));
//		suite.addTest(RefactoringTester.suite("ExtractTTPFunctionRefactoringTest", "testconfig/type/ExtractTTPStructAndDefaultTemplate.rts"));
//		suite.addTest(RefactoringTester.suite("ExtractTTPFunctionRefactoringTest", "testconfig/type/ExtractTTPStructAndDefaultTemplateNamed.rts"));
//		suite.addTest(RefactoringTester.suite("ExtractTTPFunctionRefactoringTest", "testconfig/type/ExtractTTPStructAndDefaultTemplateTemplateType.rts"));
//		suite.addTest(RefactoringTester.suite("ExtractTTPFunctionRefactoringTest", "testconfig/type/ExtractTTPStructTemplateTypeAlreadyTemplate.rts"));
		return suite;
	}

	/**
	 * Creates a {@link JUnit4TestAdapter} for a given test class.
	 * @param cl Class of the test class.
	 * @return Test adapter.
	 */
	private static JUnit4TestAdapter createAdapter(Class<?> cl) {
		return new JUnit4TestAdapter(cl);
	}
}
