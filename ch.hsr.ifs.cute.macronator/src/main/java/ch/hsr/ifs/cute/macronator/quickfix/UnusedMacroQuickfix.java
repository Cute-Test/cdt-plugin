package ch.hsr.ifs.cute.macronator.quickfix;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;

import ch.hsr.ifs.cute.macronator.transform.MacroTransformation;

public class UnusedMacroQuickfix extends MacroQuickFix {

    @Override
    public String getLabel() {
        return "remove macro";
    }

    @Override
    public void apply(IASTPreprocessorMacroDefinition macroDefinition) {
        this.applyTransformation(new MacroTransformation(macroDefinition) {
            @Override
            public boolean isValid() {
                return true;
            }

            @Override
            protected String generateTransformationCode() {
                return "";
            }
        });
    }
}
