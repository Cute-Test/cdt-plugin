package ch.hsr.ifs.cute.macronator.quickfix;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;

import ch.hsr.ifs.cute.macronator.transform.MacroTransformation;

public class UnusedMacroQuickfix extends MacroQuickFix {

    @Override
    public String getLabel() {
        return "remove macro";
    }

    @Override
    public void apply(final IASTPreprocessorMacroDefinition macroDefinition) {
        this.applyTransformation(macroDefinition, new MacroTransformation(() -> ""));
    }
}
