package ch.hsr.ifs.cute.macronator.test.transform;

import static ch.hsr.ifs.cute.macronator.test.testutils.TestUtils.createFunctionStyleMacroDefinition;
import static ch.hsr.ifs.cute.macronator.test.testutils.TestUtils.createMacroDefinition;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import ch.hsr.ifs.cute.macronator.transform.AutoFunctionTransformation;
import ch.hsr.ifs.cute.macronator.transform.ConstexprTransformation;
import ch.hsr.ifs.cute.macronator.transform.MacroTransformation;
import ch.hsr.ifs.cute.macronator.transform.VoidFunctionTransformation;

@RunWith(Parameterized.class)
public class BuiltinMacroTest {

	MacroTransformation transformation;

    public BuiltinMacroTest(MacroTransformation transformation) {
		this.transformation = transformation;

    }

    @Parameterized.Parameters
    public static Collection<Object[]> configs() {
    	MacroTransformation constexprContaining__FILE__ = new ConstexprTransformation(createMacroDefinition("#define MACRO __FILE__"));
    	MacroTransformation constexprContaining__LINE__ = new ConstexprTransformation(createMacroDefinition("#define MACRO __LINE__"));
    	MacroTransformation autoFunctionContaining__FILE__ =  new AutoFunctionTransformation(createFunctionStyleMacroDefinition("#define MACRO(A) __FILE__"));
    	MacroTransformation autoFunctionContaining__LINE__ =  new AutoFunctionTransformation(createFunctionStyleMacroDefinition("#define MACRO(A) __LINE__"));
    	MacroTransformation voidFunctionContaining__FILE__ =  new VoidFunctionTransformation(createFunctionStyleMacroDefinition("#define MACRO(X) do {__FILE__;} while(0);"));
    	MacroTransformation voidFunctionContaining__LINE__ =  new VoidFunctionTransformation(createFunctionStyleMacroDefinition("#define MACRO(X) do {__LINE__;} while(0);"));

        return Arrays.asList(new Object[][]{
        		{constexprContaining__FILE__},
        		{constexprContaining__LINE__},
        		{autoFunctionContaining__FILE__},
        		{autoFunctionContaining__LINE__},
        		{voidFunctionContaining__LINE__},
        		{voidFunctionContaining__FILE__}
        });
    }

    @Test
    public void testTransformationShouldBeInvalidIfContains__LINE__or__FILE__() throws Exception {
    	assertFalse(transformation.isValid());
    }
}
