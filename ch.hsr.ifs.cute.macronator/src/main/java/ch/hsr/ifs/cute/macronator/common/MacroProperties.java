package ch.hsr.ifs.cute.macronator.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.cute.macronator.MacronatorPlugin;

public class MacroProperties {

    private final IASTPreprocessorMacroDefinition macroDefinition;

    public MacroProperties(IASTPreprocessorMacroDefinition macroDefinition) {
        this.macroDefinition = macroDefinition;
    }

    public boolean isObjectStyle() {
        /*
         * this check has to has to be done this way, because
         * IASTFunctionStyleMacroDefintion inherits from
         * ObjectStyleMacroDefinition :S
         */
        return !(macroDefinition instanceof IASTPreprocessorFunctionStyleMacroDefinition);
    }

    public List<String> getFreeVariables() {
        List<String> freeVariables = new ArrayList<String>();
        String expansion = macroDefinition.getExpansion();
        LexerAdapter lexerAdapter = new LexerAdapter(expansion);
        while (!lexerAdapter.atEndOfInput()) {
            IToken token = lexerAdapter.nextToken();
            if (token.getType() == IToken.tIDENTIFIER) {
                if (!freeVariables.contains(token.getImage())) {
                    freeVariables.add(token.getImage());
                }
            }
        }
        freeVariables.removeAll(getParameters());
        return freeVariables;
    }

    private List<String> getParameters() {
        if (isObjectStyle()) {
            return Collections.emptyList();
        }
        List<String> parameters = new ArrayList<String>();
        for (IASTFunctionStyleMacroParameter parameter : ((IASTPreprocessorFunctionStyleMacroDefinition) macroDefinition).getParameters()) {
            parameters.add(parameter.getParameter());
        }
        return parameters;
    }

    public boolean suggestionsSuppressed() {
        IProject project = macroDefinition.getTranslationUnit().getOriginatingTranslationUnit().getCProject().getProject();
        SuppressedMacros suppressedMacros = new SuppressedMacros(project);
        return suppressedMacros.isSuppressed(macroDefinition.getName().toString());
    }

    public IIndexName[] getReferences() {
        try {
            IIndex index = macroDefinition.getTranslationUnit().getIndex();
            IIndexBinding binding = index.findBinding(macroDefinition.getName());
            return index.findNames(binding, IIndex.FIND_REFERENCES);
        } catch (CoreException e) {
            MacronatorPlugin.log(e, "could not obtain macro references");
        }
        return new IIndexName[0];
    }
}
