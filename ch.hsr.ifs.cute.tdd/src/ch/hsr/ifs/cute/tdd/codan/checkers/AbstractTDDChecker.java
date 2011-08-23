package ch.hsr.ifs.cute.tdd.codan.checkers;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public abstract class AbstractTDDChecker extends AbstractIndexAstChecker {

	public void processAst(IASTTranslationUnit ast) {
//		CxxModelsCache cache = getModelCache();
//		try {
//			IIndex index = cache.getIndex();
//			if (index != null && index.getAllFiles().length > 0) {
				runChecker(ast);
//			}
//		} catch (OperationCanceledException e) {
//		} catch (CoreException e) {
//		}
	}

	protected abstract void runChecker(IASTTranslationUnit ast);

}