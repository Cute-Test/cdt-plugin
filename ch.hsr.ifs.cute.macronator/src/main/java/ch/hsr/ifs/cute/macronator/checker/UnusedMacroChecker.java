package ch.hsr.ifs.cute.macronator.checker;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import ch.hsr.ifs.cute.macronator.common.MacroProperties;

public class UnusedMacroChecker extends AbstractIndexAstChecker {

	public static final String PROBLEM_ID = "ch.hsr.ifs.macronator.plugin.UnusedMacro";

	@Override
	public void processAst(IASTTranslationUnit ast) {
		for (IASTPreprocessorMacroDefinition macro : ast.getMacroDefinitions()) {
			MacroProperties properties = new MacroProperties(macro);
			if (isNeverUsed(macro) && macro.isActive() && !properties.suggestionsSuppressed()) {
				reportProblem(PROBLEM_ID, macro);
			}
		}
	}

	private boolean isNeverUsed(IASTPreprocessorMacroDefinition macro) {
		return new MacroProperties(macro).getReferences().length == 0;
	}
}
