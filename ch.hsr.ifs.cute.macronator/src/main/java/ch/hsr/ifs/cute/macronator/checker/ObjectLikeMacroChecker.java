package ch.hsr.ifs.cute.macronator.checker;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ITranslationUnit;

import ch.hsr.ifs.cute.macronator.common.MacroClassifier;
import ch.hsr.ifs.cute.macronator.common.MacroProperties;
import ch.hsr.ifs.cute.macronator.transform.ConstexprTransformer;
import ch.hsr.ifs.cute.macronator.transform.MacroTransformation;

public class ObjectLikeMacroChecker extends AbstractIndexAstChecker {

    public static final String PROBLEM_ID = "ch.hsr.ifs.macronator.plugin.ObsoleteObjectLikeMacro";

    @Override
    public void processAst(final IASTTranslationUnit ast) {
        final Map<ITranslationUnit, IASTTranslationUnit> astCache = new HashMap<>();
        for (final IASTPreprocessorMacroDefinition macro : ast.getMacroDefinitions()) {
            final MacroClassifier classifier = new MacroClassifier(macro, astCache);
            final MacroProperties properties = new MacroProperties(macro);
            if (classifier.isObjectLike() 
                    && classifier.areDependenciesValid() 
                    && new MacroTransformation(new ConstexprTransformer(macro)).isValid() 
                    && !properties.suggestionsSuppressed()) {
                reportProblem(PROBLEM_ID, macro);
            }
        }

    }
}
