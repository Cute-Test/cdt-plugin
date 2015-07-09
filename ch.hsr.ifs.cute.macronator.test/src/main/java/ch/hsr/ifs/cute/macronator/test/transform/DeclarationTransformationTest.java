package ch.hsr.ifs.cute.macronator.test.transform;

import static ch.hsr.ifs.cute.macronator.test.testutils.TestUtils.assertTransformationEquals;
import static ch.hsr.ifs.cute.macronator.test.testutils.TestUtils.createFunctionStyleMacroDefinition;
import static org.junit.Assert.*;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.junit.Test;

import ch.hsr.ifs.cute.macronator.transform.DeclarationTransformation;

public class DeclarationTransformationTest {
	
	@Test
	public void testShouldBeValidIfMacroExpandsToDeclaration() {
		IASTPreprocessorFunctionStyleMacroDefinition declarationMacro = createFunctionStyleMacroDefinition("#define DEF(X, Y) X Y;");
		assertTrue(new DeclarationTransformation(declarationMacro).isValid());
	}
	
	@Test
	public void testShouldBeInvalidIfMacroExpansionIsMissingSemicolon() {
		IASTPreprocessorFunctionStyleMacroDefinition declarationMacro = createFunctionStyleMacroDefinition("#define DEF(X, Y) X Y");
		assertFalse(new DeclarationTransformation(declarationMacro).isValid());
	}
	
	@Test
	public void testShouldGenerateCorrectTransformationIfMacroExpandsToDeclaration() {
		IASTPreprocessorFunctionStyleMacroDefinition declarationMacro = createFunctionStyleMacroDefinition("#define DEF(X, Y) X Y;");
		String expectedTransformation = "X Y;";
		assertTransformationEquals(expectedTransformation, new DeclarationTransformation(declarationMacro));
	}
	
	@Test
	public void testShouldBeInvalidIfMacroExpandsToStatement() {
		IASTPreprocessorFunctionStyleMacroDefinition statementMacro = createFunctionStyleMacroDefinition("#define DO(X) do {X;} while(0)");
		assertFalse( new DeclarationTransformation(statementMacro).isValid());
	}

	@Test
	public void testShouldBeInvalidIfMacroExpandsToExpression() {
		IASTPreprocessorFunctionStyleMacroDefinition expressionMacro = createFunctionStyleMacroDefinition("#define DO(X) (X) * (X)");
		assertFalse(new DeclarationTransformation(expressionMacro).isValid());
	}
}
