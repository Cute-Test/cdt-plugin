package ch.hsr.ifs.cute.macronator.test.transform;

import static ch.hsr.ifs.cute.macronator.test.testutils.TestUtils.assertTransformationEquals;
import static ch.hsr.ifs.cute.macronator.test.testutils.TestUtils.createFunctionStyleMacroDefinition;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.junit.Test;

import ch.hsr.ifs.cute.macronator.transform.DeclarationTransformer;
import ch.hsr.ifs.cute.macronator.transform.MacroTransformation;

public class DeclarationTransformationTest {
	
	@Test
	public void testShouldBeValidIfMacroExpandsToDeclaration() {
		final IASTPreprocessorFunctionStyleMacroDefinition declarationMacro = createFunctionStyleMacroDefinition("#define DEF(X, Y) X Y;");
		assertTrue(createTransformation(declarationMacro).isValid());
	}
	
	@Test
	public void testShouldBeInvalidIfMacroExpansionIsMissingSemicolon() {
		final IASTPreprocessorFunctionStyleMacroDefinition declarationMacro = createFunctionStyleMacroDefinition("#define DEF(X, Y) X Y");
		assertFalse(createTransformation(declarationMacro).isValid());
	}
	
	@Test
	public void testShouldGenerateCorrectTransformationIfMacroExpandsToDeclaration() {
		final IASTPreprocessorFunctionStyleMacroDefinition declarationMacro = createFunctionStyleMacroDefinition("#define DEF(X, Y) X Y;");
		final String expectedTransformation = "X Y;";
		assertTransformationEquals(expectedTransformation, createTransformation(declarationMacro));
	}

    private MacroTransformation createTransformation(final IASTPreprocessorFunctionStyleMacroDefinition declarationMacro) {
        return new MacroTransformation(new DeclarationTransformer(declarationMacro));
    }
	
	@Test
	public void testShouldBeInvalidIfMacroExpandsToStatement() {
		final IASTPreprocessorFunctionStyleMacroDefinition statementMacro = createFunctionStyleMacroDefinition("#define DO(X) do {X;} while(0)");
		assertFalse(createTransformation(statementMacro).isValid());
	}

	@Test
	public void testShouldBeInvalidIfMacroExpandsToExpression() {
		final IASTPreprocessorFunctionStyleMacroDefinition expressionMacro = createFunctionStyleMacroDefinition("#define DO(X) (X) * (X)");
		assertFalse(createTransformation(expressionMacro).isValid());
	}
}
