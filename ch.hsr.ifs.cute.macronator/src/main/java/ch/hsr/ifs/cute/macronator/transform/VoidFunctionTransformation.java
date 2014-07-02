package ch.hsr.ifs.cute.macronator.transform;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;

public class VoidFunctionTransformation extends ParameterizedExpressionTransformation {

    public VoidFunctionTransformation(IASTPreprocessorFunctionStyleMacroDefinition macro) {
        super(macro);
    }

    @Override
    protected String generateTransformationCode() {
        final StringBuilder transformation = new StringBuilder();
        transformation.append(generateTypenames(getFunctionStyleMacroDefinition().getParameters()));
        transformation.append(" inline void ");
        transformation.append(getMacroDefinition().getName().toString());
        transformation.append(generateFunctionParameters(getFunctionStyleMacroDefinition().getParameters()));
        transformation.append("{");
        transformation.append(getMacroDefinition().getExpansion());
        if (!getMacroDefinition().getExpansion().endsWith(";") && !getMacroDefinition().getExpansion().endsWith("}")) {
            transformation.append(";");
        }
        return transformation.append("}").toString();
    }
}
