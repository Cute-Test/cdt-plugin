package ch.hsr.ifs.cute.macronator.transform;

import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.parser.IToken;

public class ConstexprTransformation extends MacroTransformation {

    public ConstexprTransformation(IASTPreprocessorMacroDefinition macro) {
        super(macro);
    }

    @Override
    protected String generateTransformationCode() {
        try {
            IToken token = macro.getSyntax().getNext().getNext(); // skip
                                                                  // '#define'
            StringBuilder replacementText = new StringBuilder(String.format("constexpr auto %s =", token.getImage()));
            token = token.getNext();
            while (token != null) {
                replacementText.append(token.getImage() + " ");
                token = token.getNext();
            }
            replacementText.append(";");
            return replacementText.toString();
        } catch (ExpansionOverlapsBoundaryException e) {
            throw new RuntimeException(e);
        }
    }
}
