package ch.hsr.ifs.cute.macronator.transform;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;

public class AutoFunctionTransformation extends ParameterizedExpressionTransformation {

    public AutoFunctionTransformation(final IASTPreprocessorFunctionStyleMacroDefinition macroDefinition) {
        super(macroDefinition);
    }

    @Override
    public String generateTransformationCode() {
        final StringBuilder transformation = new StringBuilder();
        transformation.append(generateTypenames(getFunctionStyleMacroDefinition().getParameters()));
        transformation.append("constexpr inline auto ");
        transformation.append(getFunctionStyleMacroDefinition().getName().toString());
        transformation.append(generateFunctionParameters(getFunctionStyleMacroDefinition().getParameters()));
        transformation.append(" -> decltype(" + getFunctionStyleMacroDefinition().getExpansion() + "){");
        transformation.append("return (" + getFunctionStyleMacroDefinition().getExpansion() + "); }");
        return transformation.toString();
    }
}
