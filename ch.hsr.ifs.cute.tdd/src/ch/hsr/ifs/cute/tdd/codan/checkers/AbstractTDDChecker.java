package ch.hsr.ifs.cute.tdd.codan.checkers;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public abstract class AbstractTDDChecker extends AbstractIndexAstChecker {

	@Override
	public void processAst(IASTTranslationUnit ast) {
		runChecker(ast);
	}

	protected abstract void runChecker(IASTTranslationUnit ast);

}