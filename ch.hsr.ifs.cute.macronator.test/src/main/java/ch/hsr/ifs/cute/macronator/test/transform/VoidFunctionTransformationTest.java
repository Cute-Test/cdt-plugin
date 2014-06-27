package ch.hsr.ifs.cute.macronator.test.transform;

import static ch.hsr.ifs.cute.macronator.test.testutils.TestUtils.assertTransformationEquals;
import static ch.hsr.ifs.cute.macronator.test.testutils.TestUtils.createFunctionStyleMacroDefinition;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.hsr.ifs.cute.macronator.transform.VoidFunctionTransformation;

public class VoidFunctionTransformationTest {
	
	@Test
	public void testShouldProduceCorrectVoidFunctionCodeForParameterizedExpressionWithOneParameter() {
		String macro = "#define MACRO(A) (X)";
		String macroTransformation = "template<typename T1> inline void MACRO(T1&& A) {(X);}";
		assertTransformationEquals(macroTransformation, new VoidFunctionTransformation(createFunctionStyleMacroDefinition(macro)));
	}
	
	@Test
	public void testTransformationShouldBeValidIfTransformationIsPossible() {
		String macro = "#define MACRO(A) (X)";
		assertTrue(new VoidFunctionTransformation(createFunctionStyleMacroDefinition(macro)).isValid());
	}
	
	@Test
	public void testTransformationShouldBeValidIfTransformationIsTheEmptyString() {
		String macro = "#define MACRO(A) ";
		assertTrue(new VoidFunctionTransformation(createFunctionStyleMacroDefinition(macro)).isValid());
	}
	
	@Test
	public void testTransformationIsValidShouldBeFalseIfReplacementTextSyntaxIsIncorrect() {
		String macro = "#define MACRO(A) if (";
		assertFalse(new VoidFunctionTransformation(createFunctionStyleMacroDefinition(macro)).isValid());
	}
	
	@Test
	public void testShouldProduceCorrectVoidFunctionCodeForParameterizedExpressionWithTwoParameters() {
		String macro = "#define MACRO(A, B) (X)";
		String macroTransformation = "template<typename T1, typename T2> inline void MACRO(T1&& A, T2&& B) {(X);}";
		assertTransformationEquals(macroTransformation, new VoidFunctionTransformation(createFunctionStyleMacroDefinition(macro)));
	}
	
	@Test
	public void testShouldProduceCorrectVoidFunctionForParameterizedExpressionWithThreeParameters() {
		String macro = "#define MACRO(A, B, C) (X)";
		String macroTransformation = "template<typename T1, typename T2, typename T3> inline void MACRO(T1&& A, T2&& B, T3&& C) {(X);}";
		assertTransformationEquals(macroTransformation, new VoidFunctionTransformation(createFunctionStyleMacroDefinition(macro)));
	}
	
	@Test
	public void testShouldProduceCorrectVoidFunctionForDoWhileStatement() {
		String macro = "#define MACRO(X) do { X; } while (0)";
		String macroTransformation = "template<typename T1> inline void MACRO(T1&& X) {do { X; } while (0);}";
		assertTransformationEquals(macroTransformation, new VoidFunctionTransformation(createFunctionStyleMacroDefinition(macro)));
	}
	
	@Test
	public void testShouldNotAddSemicolonToStatementIfAlreadyExisting() throws Exception {
		String macro = "#define MACRO(X) do {X;} while(0);";
		String macroTransformation = "template<typename T1> inline void MACRO(T1&& X) {do { X; } while (0);}";
		assertTransformationEquals(macroTransformation, new VoidFunctionTransformation(createFunctionStyleMacroDefinition(macro)));
	}
}