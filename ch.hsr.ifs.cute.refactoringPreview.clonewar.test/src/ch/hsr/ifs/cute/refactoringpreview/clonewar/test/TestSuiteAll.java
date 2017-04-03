package ch.hsr.ifs.cute.refactoringpreview.clonewar.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.cute.refactoringpreview.clonewar.test.tests.ExtractClassTemplateTest;
import ch.hsr.ifs.cute.refactoringpreview.clonewar.test.tests.ExtractFunctionTemplateTest;

@RunWith(Suite.class)
@SuiteClasses({
	ExtractFunctionTemplateTest.class,
	ExtractClassTemplateTest.class
})
public class TestSuiteAll {

}
