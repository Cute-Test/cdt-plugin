package ch.hsr.ifs.cute.macronator.checker;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ITranslationUnit;

import ch.hsr.ifs.cute.macronator.common.MacroClassifier;
import ch.hsr.ifs.cute.macronator.common.MacroProperties;
import ch.hsr.ifs.cute.macronator.transform.AutoFunctionTransformer;
import ch.hsr.ifs.cute.macronator.transform.DeclarationTransformer;
import ch.hsr.ifs.cute.macronator.transform.MacroTransformation;
import ch.hsr.ifs.cute.macronator.transform.VoidFunctionTransformer;

public class FunctionLikeMacroChecker extends AbstractIndexAstChecker {

    public static final String PROBLEM_ID = "ch.hsr.ifs.macronator.plugin.ObsoleteFunctionLikeMacro";

    @Override
    public void processAst(final IASTTranslationUnit ast) {
        final Map<ITranslationUnit, IASTTranslationUnit> astCache = new HashMap<>();
        astCache.put(ast.getOriginatingTranslationUnit(), ast);
        for (final IASTPreprocessorMacroDefinition macro : ast.getMacroDefinitions()) {
            final MacroClassifier classifier = new MacroClassifier(macro, astCache);
            final MacroProperties properties = new MacroProperties(macro);
            if (classifier.isFunctionLike() 
                    && isTransformationValid((IASTPreprocessorFunctionStyleMacroDefinition) macro) 
                    && classifier.areDependenciesValid() 
                    && !properties.suggestionsSuppressed()) {
                reportProblem(PROBLEM_ID, macro);
            }
        }
    }

    private boolean isTransformationValid(final IASTPreprocessorFunctionStyleMacroDefinition macro) {
        return isTransformableToFunction(macro) && !new DeclarationTransformer(macro).isValid();
    }

    private boolean isTransformableToFunction(final IASTPreprocessorFunctionStyleMacroDefinition macro) {
        return new MacroTransformation(new AutoFunctionTransformer(macro)).isValid() || new MacroTransformation(new VoidFunctionTransformer(macro)).isValid();
    }
}
