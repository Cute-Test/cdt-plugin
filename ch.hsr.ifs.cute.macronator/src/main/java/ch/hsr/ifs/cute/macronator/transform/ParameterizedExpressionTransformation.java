package ch.hsr.ifs.cute.macronator.transform;

import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;

public abstract class ParameterizedExpressionTransformation extends MacroTransformation {

    private final IASTPreprocessorFunctionStyleMacroDefinition macro;

    public ParameterizedExpressionTransformation(final IASTPreprocessorFunctionStyleMacroDefinition macro) {
        super(macro);
        this.macro = macro;
    }

    protected String generateFunctionParameters(IASTFunctionStyleMacroParameter[] parameters) {
        String functionParameters = "(";
        for (int i = 0; i < parameters.length - 1; i++) {
            functionParameters += "T" + (i + 1) + "&& " + parameters[i].getParameter() + ",";
        }
        functionParameters += "T" + parameters.length + "&& " + parameters[parameters.length - 1].getParameter() + ")";
        return functionParameters;
    }

    protected String generateTypenames(IASTFunctionStyleMacroParameter[] parameters) {
        String typenames = "<";
        for (int i = 0; i < parameters.length - 1; i++) {
            typenames += "typename T" + (i + 1) + ",";
        }
        typenames += "typename T" + parameters.length + ">";
        return typenames;
    }

    protected IASTPreprocessorFunctionStyleMacroDefinition getFunctionStyleMacroDefinition() {
        return macro;
    }
}