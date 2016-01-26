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
    public void apply(final IASTPreprocessorMacroDefinition macroDefinition) {
        if (!(macroDefinition instanceof IASTPreprocessorFunctionStyleMacroDefinition)) {
            MacronatorPlugin.getDefault().getLog().log(new Status(IStatus.WARNING, MacronatorPlugin.PLUGIN_ID, "No macro definiton found"));
            return;
        }

        final IASTPreprocessorFunctionStyleMacroDefinition functionLikeMacro = (IASTPreprocessorFunctionStyleMacroDefinition) macroDefinition;      
        final MacroTransformation macroTransformation = new MacroTransformation(new AutoFunctionTransformation(functionLikeMacro));
        if (macroTransformation.isValid()) {
            applyTransformation(macroDefinition, macroTransformation);
            return;
        } 
        
        final MacroTransformation voidFunctionTransformation = new MacroTransformation(new VoidFunctionTransformation(functionLikeMacro));
        if (voidFunctionTransformation.isValid()) {
            applyTransformation(macroDefinition, voidFunctionTransformation);
        }
    }
}
