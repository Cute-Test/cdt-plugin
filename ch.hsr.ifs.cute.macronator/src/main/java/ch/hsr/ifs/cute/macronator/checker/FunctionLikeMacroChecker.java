package ch.hsr.ifs.cute.macronator.checker;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import ch.hsr.ifs.cute.macronator.common.MacroClassifier;
import ch.hsr.ifs.cute.macronator.common.MacroProperties;
import ch.hsr.ifs.cute.macronator.transform.AutoFunctionTransformation;
import ch.hsr.ifs.cute.macronator.transform.DeclarationTransformation;
import ch.hsr.ifs.cute.macronator.transform.MacroTransformation;
import ch.hsr.ifs.cute.macronator.transform.VoidFunctionTransformation;

public class FunctionLikeMacroChecker extends AbstractIndexAstChecker {

	public static final String PROBLEM_ID = "ch.hsr.ifs.macronator.plugin.ObsoleteFunctionLikeMacro";

	@Override
	public void processAst(final IASTTranslationUnit translationUnit) {

		for (final IASTPreprocessorMacroDefinition macro : translationUnit.getMacroDefinitions()) {
			final MacroClassifier classifier = new MacroClassifier(macro);
			final MacroProperties properties = new MacroProperties(macro);
			if (classifier.isFunctionLike() && classifier.areDependenciesValid()
					&& isTransformationValid((IASTPreprocessorFunctionStyleMacroDefinition) macro)
					&& !properties.suggestionsSuppressed()) {
				reportProblem(PROBLEM_ID, macro);
			}
		}
	}

	private boolean isTransformationValid(final IASTPreprocessorFunctionStyleMacroDefinition macro) {
		return isTransformableToFunction(macro) && !new DeclarationTransformation(macro).isValid();
	}

    private boolean isTransformableToFunction(final IASTPreprocessorFunctionStyleMacroDefinition macro) {
        return new MacroTransformation(new AutoFunctionTransformation(macro)).isValid() || new MacroTransformation(new VoidFunctionTransformation(macro)).isValid();
    }
}
