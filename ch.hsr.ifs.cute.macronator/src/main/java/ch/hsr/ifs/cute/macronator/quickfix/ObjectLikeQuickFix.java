package ch.hsr.ifs.cute.macronator.quickfix;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.core.resources.IMarker;

import ch.hsr.ifs.cute.macronator.transform.ConstexprTransformer;
import ch.hsr.ifs.cute.macronator.transform.MacroTransformation;

public class ObjectLikeQuickFix extends MacroQuickFix {

    @Override
    public void apply(final IASTPreprocessorMacroDefinition macroDefinition) {
        applyTransformation(macroDefinition, new MacroTransformation(new ConstexprTransformer(macroDefinition)));
    }

    @Override
    public String getLabel() {
        return "Replace macro definition with 'constexpr' expression";
    }

    @Override
    public boolean isApplicable(final IMarker marker) {
        return true;
    }
}