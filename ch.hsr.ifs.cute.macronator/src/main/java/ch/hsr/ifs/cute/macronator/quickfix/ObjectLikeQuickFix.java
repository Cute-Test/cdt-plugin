package ch.hsr.ifs.cute.macronator.quickfix;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.core.resources.IMarker;

import ch.hsr.ifs.cute.macronator.transform.ConstexprTransformation;

public class ObjectLikeQuickFix extends MacroQuickFix {

    @Override
    public void apply(IASTPreprocessorMacroDefinition macroDefinition) {
        applyTransformation(new ConstexprTransformation(macroDefinition));
    }

    @Override
    public String getLabel() {
        return "Replace macro definition with 'constexpr' expression";
    }

    @Override
    public boolean isApplicable(IMarker marker) {
        return true;
    }
}