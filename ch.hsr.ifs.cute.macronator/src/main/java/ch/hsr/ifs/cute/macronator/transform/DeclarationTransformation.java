package ch.hsr.ifs.cute.macronator.transform;

import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;

public class DeclarationTransformation extends ParameterizedExpressionTransformation {

    public DeclarationTransformation(final IASTPreprocessorFunctionStyleMacroDefinition macro) {
        super(macro);
    }

    @Override
    protected String generateTransformationCode() {
        String type = getFunctionStyleMacroDefinition().getParameters()[0].getParameter();
        String name = getFunctionStyleMacroDefinition().getParameters()[1].getParameter();
        return String.format("%s %s;", type, name);
    }

    private boolean endsWithSemicolon() {
        return getMacroDefinition().getExpansion().endsWith(";");
    }

    @Override
    public boolean isValid() {
        IASTFunctionStyleMacroParameter[] parameters = getFunctionStyleMacroDefinition().getParameters();
        return (parameters != null && parameters.length == 2 && endsWithSemicolon());
    }
}