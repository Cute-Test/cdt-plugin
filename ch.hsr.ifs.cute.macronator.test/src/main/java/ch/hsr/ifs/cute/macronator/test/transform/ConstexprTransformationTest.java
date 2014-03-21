package ch.hsr.ifs.cute.macronator.test.transform;

import static ch.hsr.ifs.cute.macronator.test.testutils.TestUtils.assertTransformationEquals;
import static ch.hsr.ifs.cute.macronator.test.testutils.TestUtils.createMacroDefinition;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.junit.Test;

import ch.hsr.ifs.cute.macronator.transform.ConstexprTransformation;

public class ConstexprTransformationTest {
	
	@Test
	public void testShouldTranslateSimpleObjectMacrosLikeCorrectly() throws Exception {
		IASTPreprocessorMacroDefinition macro = createMacroDefinition("#define PI 3.1415");
		assertTransformationEquals("constexpr auto PI = 3.1415;", new ConstexprTransformation(macro));		
	}
	
	@Test
	public void testShouldTranslateObjectLikeMacrosContainingArithmeticOperators() throws Exception {
		IASTPreprocessorMacroDefinition macro = createMacroDefinition("#define TWO 1 + 1");
		assertTransformationEquals("constexpr auto TWO = 1 + 1;", new ConstexprTransformation(macro));
	}
	
	@Test
	public void testShouldTranslateObjectLikeMacrosContainingMacroIdentifiers() throws Exception {
		IASTPreprocessorMacroDefinition macro = createMacroDefinition("#define TWO ONE + ONE");
		assertTransformationEquals("constexpr auto TWO = ONE + ONE;", new ConstexprTransformation(macro));
	}
	
	@Test
	public void testShouldTranslateArithmeticExpressionCorrectly() throws Exception {
		IASTPreprocessorMacroDefinition macro = createMacroDefinition("#define CALCULATION 1 + (1 - 1)");
		assertTransformationEquals("constexpr auto CALCULATION = 1 + (1 - 1);",new ConstexprTransformation(macro));		
	}		
}
