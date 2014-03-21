package ch.hsr.ifs.cute.macronator.transform;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;

public class AutoFunctionTransformation extends ParameterizedExpressionTransformation {

    public AutoFunctionTransformation(IASTPreprocessorFunctionStyleMacroDefinition macroDefinition) {
        super(macroDefinition);
    }

    @Override
    protected String generateTransformationCode() {
        String transformation = "";
        transformation += "template ";
        transformation += generateTypenames(getFunctionStyleMacroDefinition().getParameters());
        transformation += "constexpr inline auto ";
        transformation += getMacroDefinition().getName().toString();
        transformation += generateFunctionParameters(getFunctionStyleMacroDefinition().getParameters());
        transformation += " -> decltype(" + getMacroDefinition().getExpansion() + "){";
        transformation += "return (" + getMacroDefinition().getExpansion() + ");";
        transformation += "}";
        return transformation;
    }
}
