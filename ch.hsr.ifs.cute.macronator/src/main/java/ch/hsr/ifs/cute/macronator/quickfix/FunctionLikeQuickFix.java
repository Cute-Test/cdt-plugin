package ch.hsr.ifs.cute.macronator.quickfix;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ch.hsr.ifs.cute.macronator.MacronatorPlugin;
import ch.hsr.ifs.cute.macronator.transform.AutoFunctionTransformation;
import ch.hsr.ifs.cute.macronator.transform.MacroTransformation;
import ch.hsr.ifs.cute.macronator.transform.VoidFunctionTransformation;

public class FunctionLikeQuickFix extends MacroQuickFix {

    @Override
    public String getLabel() {
        return "Replace macro definition with inline function";
    }

    @Override
    public void apply(IASTPreprocessorMacroDefinition macroDefinition) {
        if (!(macroDefinition instanceof IASTPreprocessorFunctionStyleMacroDefinition)) {
            MacronatorPlugin.getDefault().getLog().log(new Status(IStatus.WARNING, MacronatorPlugin.PLUGIN_ID, "No macro definiton found"));
            return;
        }

        MacroTransformation autoFunctionTransformation = new AutoFunctionTransformation((IASTPreprocessorFunctionStyleMacroDefinition) macroDefinition);
        if (autoFunctionTransformation.isValid()) {
            applyTransformation(autoFunctionTransformation);
            return;
        }

        MacroTransformation voidFunctionTransformation = new VoidFunctionTransformation((IASTPreprocessorFunctionStyleMacroDefinition) macroDefinition);
        if (voidFunctionTransformation.isValid()) {
            applyTransformation(voidFunctionTransformation);
        }
    }
}
