package ch.hsr.ifs.cute.macronator.transform;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;

public class VoidFunctionTransformer extends ParameterizedExpressionTransformer {

    public VoidFunctionTransformer(final IASTPreprocessorFunctionStyleMacroDefinition macro) {
        super(macro);
    }

    @Override
    public String generateTransformationCode() {
        final StringBuilder transformation = new StringBuilder();
        transformation.append(generateTypenames(getFunctionStyleMacroDefinition().getParameters()));
        transformation.append(" inline void ");
        transformation.append(getFunctionStyleMacroDefinition().getName().toString());
        transformation.append(generateFunctionParameters(getFunctionStyleMacroDefinition().getParameters()));
        transformation.append("{");
        transformation.append(getFunctionStyleMacroDefinition().getExpansion());
        if (!getFunctionStyleMacroDefinition().getExpansion().endsWith(";") && !getFunctionStyleMacroDefinition().getExpansion().endsWith("}")) {
            transformation.append(";");
        }
        return transformation.append("}").toString();
    }
}
