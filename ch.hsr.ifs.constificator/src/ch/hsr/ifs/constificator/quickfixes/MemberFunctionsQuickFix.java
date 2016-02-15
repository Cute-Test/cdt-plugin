package ch.hsr.ifs.constificator.quickfixes;

import static ch.hsr.ifs.constificator.core.util.type.Cast.as;

import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.ltk.core.refactoring.Change;

import ch.hsr.ifs.constificator.core.util.ast.DOM;

public class MemberFunctionsQuickFix extends QuickFix {

	@Override
	public String getLabel() {
		return "Add const-qualification";
	}


	@Override
	protected void handleNode(final IASTNode node, final IIndex index) {
		ICPPASTFunctionDeclarator declarator;
		if ((declarator = as(ICPPASTFunctionDeclarator.class, node)) == null) {
			return;
		}

		final IIndexBinding adapted = index.adaptBinding(declarator.getName().resolveBinding());

		final ICProject project = declarator.getTranslationUnit().getOriginatingTranslationUnit().getCProject();
		final Set<ICPPASTFunctionDeclarator> decls = DOM.resolveBindingToNodeSet(ICPPASTFunctionDeclarator.class, adapted,
				index, project);

		final RewriteCache cache = new RewriteCache();

		hasMultipleChanges = decls.size() > 1;

		for (final ICPPASTFunctionDeclarator decl : decls) {
			final ASTRewrite rewrite = cache.get(decl.getTranslationUnit());
			final ICPPASTFunctionDeclarator replacement = decl.copy();
			replacement.setConst(true);

			DOM.changed(decl.getTranslationUnit());
			rewrite.replace(decl, replacement, null);
		}

		for(final Change change : cache) {
			addChange(change);
		}
	}

}
