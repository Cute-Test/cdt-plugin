package ch.hsr.ifs.cute.macronator.transform;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;

public class VoidFunctionTransformation extends ParameterizedExpressionTransformation {

    public VoidFunctionTransformation(IASTPreprocessorFunctionStyleMacroDefinition macro) {
        super(macro);
    }

    @Override
    protected String generateTransformationCode() {
        String transformation = "template ";
        transformation += generateTypenames(getFunctionStyleMacroDefinition().getParameters());
        transformation += " inline void ";
        transformation += getMacroDefinition().getName().toString();
        transformation += generateFunctionParameters(getFunctionStyleMacroDefinition().getParameters());
        transformation += "{";
        transformation += getMacroDefinition().getExpansion();
        if (!getMacroDefinition().getExpansion().endsWith(";") && !getMacroDefinition().getExpansion().endsWith("}")) {
            transformation += ";";
        }
        return transformation += "}";
    }
}
