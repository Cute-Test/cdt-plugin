package ch.hsr.ifs.cute.macronator.transform;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;

public class AutoFunctionTransformation extends ParameterizedExpressionTransformation {

    public AutoFunctionTransformation(IASTPreprocessorFunctionStyleMacroDefinition macroDefinition) {
        super(macroDefinition);
    }

    @Override
    protected String generateTransformationCode() {
        final StringBuilder transformation = new StringBuilder();
        transformation.append(generateTypenames(getFunctionStyleMacroDefinition().getParameters()));
        transformation.append("constexpr inline auto ");
        transformation.append(getMacroDefinition().getName().toString());
        transformation.append(generateFunctionParameters(getFunctionStyleMacroDefinition().getParameters()));
        transformation.append(" -> decltype(" + getMacroDefinition().getExpansion() + "){");
        transformation.append("return (" + getMacroDefinition().getExpansion() + "); }");
        return transformation.toString();
    }
}
