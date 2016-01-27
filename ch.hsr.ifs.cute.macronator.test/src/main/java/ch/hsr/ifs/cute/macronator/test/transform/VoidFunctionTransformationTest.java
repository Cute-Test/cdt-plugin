package ch.hsr.ifs.cute.macronator.test.transform;

import static ch.hsr.ifs.cute.macronator.test.testutils.TestUtils.assertTransformationEquals;
import static ch.hsr.ifs.cute.macronator.test.testutils.TestUtils.createFunctionStyleMacroDefinition;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.hsr.ifs.cute.macronator.transform.MacroTransformation;
import ch.hsr.ifs.cute.macronator.transform.VoidFunctionTransformer;

public class VoidFunctionTransformationTest {
	
	@Test
	public void testShouldProduceCorrectVoidFunctionCodeForParameterizedExpressionWithOneParameter() {
		final String macro = "#define MACRO(A) (X)";
		final String macroTransformation = "template<typename T1> inline void MACRO(T1&& A) {(X);}";
		assertTransformationEquals(macroTransformation, createTransformation(macro));
	}

	
	@Test
	public void testTransformationShouldBeValidIfTransformationIsPossible() {
		final String macro = "#define MACRO(A) (X)";
		assertTrue(createTransformation(macro).isValid());
	}
	
	@Test
	public void testTransformationShouldBeValidIfTransformationIsTheEmptyString() {
		final String macro = "#define MACRO(A) ";
		assertTrue(createTransformation(macro).isValid());
	}
	
	@Test
	public void testTransformationIsValidShouldBeFalseIfReplacementTextSyntaxIsIncorrect() {
		final String macro = "#define MACRO(A) if (";
		assertFalse(createTransformation(macro).isValid());
	}
	
	@Test
	public void testShouldProduceCorrectVoidFunctionCodeForParameterizedExpressionWithTwoParameters() {
		final String macro = "#define MACRO(A, B) (X)";
		final String macroTransformation = "template<typename T1, typename T2> inline void MACRO(T1&& A, T2&& B) {(X);}";
		assertTransformationEquals(macroTransformation, createTransformation(macro));
	}
	
	@Test
	public void testShouldProduceCorrectVoidFunctionForParameterizedExpressionWithThreeParameters() {
		final String macro = "#define MACRO(A, B, C) (X)";
		final String macroTransformation = "template<typename T1, typename T2, typename T3> inline void MACRO(T1&& A, T2&& B, T3&& C) {(X);}";
		assertTransformationEquals(macroTransformation, createTransformation(macro));
	}
	
	@Test
	public void testShouldProduceCorrectVoidFunctionForDoWhileStatement() {
		final String macro = "#define MACRO(X) do { X; } while (0)";
		final String macroTransformation = "template<typename T1> inline void MACRO(T1&& X) {do { X; } while (0);}";
		assertTransformationEquals(macroTransformation, createTransformation(macro));
	}
	
	@Test
	public void testShouldNotAddSemicolonToStatementIfAlreadyExisting() throws Exception {
		final String macro = "#define MACRO(X) do {X;} while(0);";
		final String macroTransformation = "template<typename T1> inline void MACRO(T1&& X) {do { X; } while (0);}";
		assertTransformationEquals(macroTransformation, createTransformation(macro));
	}
	
    private MacroTransformation createTransformation(final String macro) {
        return new MacroTransformation(new VoidFunctionTransformer(createFunctionStyleMacroDefinition(macro)));
    }
}