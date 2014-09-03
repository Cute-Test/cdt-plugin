package ch.hsr.ifs.cute.macronator.transform;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriter;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ProblemRuntimeException;

import ch.hsr.ifs.cute.macronator.common.Parser;

@SuppressWarnings("restriction")
public abstract class MacroTransformation {

    protected final IASTPreprocessorMacroDefinition macro;
    private boolean transformationValid;
    private String transformationCode;
    private boolean transformed;

    public MacroTransformation(IASTPreprocessorMacroDefinition macro) {
        this.macro = macro;
        this.transformed = false;
    }

    private void transform() {
        try {
            final String transformedCode = generateTransformationCode();
            final Parser parser = new Parser(transformedCode);
            final IASTTranslationUnit translationUnit = parser.parse();
            transformationValid = !parser.encounteredErrors();
            transformationCode = (transformationValid) ? new ASTWriter().write(translationUnit) : "";
        } catch (ProblemRuntimeException e) {
            transformationValid = false;
            transformationCode = "";
        }
        transformed = true;
    }

    /**
     * Returns the transformation for the supplied macro, as a correctly
     * formatted String. If no valid transformation exists (isValid() == false),
     * the empty String is returned. valid the macro transformation or the empty
     * String if no valid transformation exists.
     * 
     * @return the transformation or the empty string
     */
    public String getCode() {
        if (!transformed) {
            transform();
        }
        return transformationCode;
    }

    /**
     * Returns the {@link IASTPreprocessorMacroDefinition} associated with this
     * macro transformation.
     * 
     * @return the associated macro definition
     */
    public IASTPreprocessorMacroDefinition getMacroDefinition() {
        return macro;
    }

    /**
     * Returns true if a valid transformation for the supplied macro exists.
     * 
     * @return true if a valid transformation exists
     */
    public boolean isValid() {
        if (!transformed) {
            transform();
        }
        return (transformationValid && !containsNotAllowedBuiltinMacros());
    }

    private boolean containsNotAllowedBuiltinMacros() {
        return (transformationCode.contains("__LINE__") || transformationCode.contains("__FILE__"));
    }

    protected abstract String generateTransformationCode();
}
