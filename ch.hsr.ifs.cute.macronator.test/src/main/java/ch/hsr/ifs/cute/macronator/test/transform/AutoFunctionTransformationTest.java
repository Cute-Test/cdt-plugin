package ch.hsr.ifs.cute.macronator.test.transform;

import static ch.hsr.ifs.cute.macronator.test.testutils.TestUtils.assertTransformationEquals;
import static ch.hsr.ifs.cute.macronator.test.testutils.TestUtils.createFunctionStyleMacroDefinition;
import static org.junit.Assert.assertFalse;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.junit.Test;

import ch.hsr.ifs.cute.macronator.transform.AutoFunctionTransformation;

public class AutoFunctionTransformationTest {
	
	@Test
	public void testShouldProduceCorrectTransformationForParameterizedExpressionWithOneParameter() {
		String macro = "#define SQUARE(X) (X) * (X)";
		String macroTranslation = "template<typename T1> inline constexpr auto SQUARE(T1&& X) -> decltype((X) * (X)){return ((X) * (X));}";
		assertTransformationEquals(macroTranslation, new AutoFunctionTransformation(createFunctionStyleMacroDefinition(macro)));		
	}
	
	@Test
	public void testShouldProduceCorrectTransformationForParameterizedExpressionWithTwoParameters() {
	    String macro = "#define ADD(A, B) ((A) + (B))";
		String expectedTransformation ="template<typename T1, typename T2> inline constexpr auto ADD(T1&& A, T2&& B) -> decltype(((A) + (B))) {return (((A) + (B)));}";		
		assertTransformationEquals(expectedTransformation, new AutoFunctionTransformation(createFunctionStyleMacroDefinition(macro)));
	}
	
	@Test
	public void testShouldProduceCorrectTransformationForParameterizedExpressionWithThreeParameters() {
		String macro = "#define MACRO(A, B, C) X\n";
		String expectedTransformation ="template<typename T1, typename T2, typename T3> inline constexpr auto MACRO(T1&& A, T2&& B, T3&& C) -> decltype(X) { return (X);}";
		assertTransformationEquals(expectedTransformation, new AutoFunctionTransformation(createFunctionStyleMacroDefinition(macro)));
		}	
	
	@Test
	public void testReturnFunctionTransformationShouldBeInvalidIfFunctionDoesntExpandIntoExpression() throws Exception {
		IASTPreprocessorFunctionStyleMacroDefinition macroDefinition = createFunctionStyleMacroDefinition("#define MACRO(A) do {x;} while(0);");
		assertFalse(new AutoFunctionTransformation(macroDefinition).isValid());
	}
}