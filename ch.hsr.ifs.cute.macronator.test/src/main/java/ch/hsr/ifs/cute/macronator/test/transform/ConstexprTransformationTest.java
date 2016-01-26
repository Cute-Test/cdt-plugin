package ch.hsr.ifs.cute.macronator.test.transform;

import static ch.hsr.ifs.cute.macronator.test.testutils.TestUtils.assertTransformationEquals;
import static ch.hsr.ifs.cute.macronator.test.testutils.TestUtils.createMacroDefinition;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.junit.Test;

import ch.hsr.ifs.cute.macronator.transform.ConstexprTransformation;
import ch.hsr.ifs.cute.macronator.transform.MacroTransformation;

public class ConstexprTransformationTest {
	
	@Test
	public void testShouldTranslateSimpleObjectMacrosLikeCorrectly() throws Exception {
		final IASTPreprocessorMacroDefinition macro = createMacroDefinition("#define PI 3.1415");
		assertTransformationEquals("constexpr auto PI = 3.1415;", createTransformation(macro));
	}

	@Test
	public void testShouldTranslateObjectLikeMacrosContainingArithmeticOperators() throws Exception {
		final IASTPreprocessorMacroDefinition macro = createMacroDefinition("#define TWO 1 + 1");
		assertTransformationEquals("constexpr auto TWO = 1 + 1;", createTransformation(macro));
	}
	
	@Test
	public void testShouldTranslateObjectLikeMacrosContainingMacroIdentifiers() throws Exception {
		final IASTPreprocessorMacroDefinition macro = createMacroDefinition("#define TWO ONE + ONE");
		assertTransformationEquals("constexpr auto TWO = ONE + ONE;", createTransformation(macro));
	}
	
	@Test
	public void testShouldTranslateArithmeticExpressionCorrectly() throws Exception {
		final IASTPreprocessorMacroDefinition macro = createMacroDefinition("#define CALCULATION 1 + (1 - 1)");
		assertTransformationEquals("constexpr auto CALCULATION = 1 + (1 - 1);",createTransformation(macro));
	}
	
    private MacroTransformation createTransformation(final IASTPreprocessorMacroDefinition macro) {
        return new MacroTransformation(new ConstexprTransformation(macro));
    }
}
