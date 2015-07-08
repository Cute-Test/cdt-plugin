package ch.hsr.ifs.cute.macronator.checker;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import ch.hsr.ifs.cute.macronator.common.MacroClassifier;
import ch.hsr.ifs.cute.macronator.common.MacroProperties;
import ch.hsr.ifs.cute.macronator.transform.ConstexprTransformation;

public class ObjectLikeMacroChecker extends AbstractIndexAstChecker {

	public static final String PROBLEM_ID = "ch.hsr.ifs.macronator.plugin.ObsoleteObjectLikeMacro";

	@Override
	public void processAst(IASTTranslationUnit translationUnit) {
		for (IASTPreprocessorMacroDefinition macro : translationUnit.getMacroDefinitions()) {
			MacroClassifier classifier = new MacroClassifier(macro);
			MacroProperties properties = new MacroProperties(macro);
			if (classifier.isObjectLike() && classifier.areDependenciesValid()
					&& new ConstexprTransformation(macro).isValid() && !properties.suggestionsSuppressed()) {
				reportProblem(PROBLEM_ID, macro);
			}
		}
	}
}
