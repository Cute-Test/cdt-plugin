package ch.hsr.ifs.cute.constificator.quickfixes;

import static ch.hsr.ifs.cute.constificator.core.util.type.Cast.as;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.cute.constificator.core.asttools.ASTRewriteCache;
import ch.hsr.ifs.cute.constificator.core.util.ast.Relation;

public class MemberFunctionsQuickFix extends QuickFix {

	@Override
	public String getLabel() {
		return "Add const-qualification";
	}


	@Override
	protected void handleNode(final IASTNode node, final IIndex index, ASTRewriteCache cache) {
		ICPPASTFunctionDeclarator declarator;
		if ((declarator = as(ICPPASTFunctionDeclarator.class, node)) == null) {
			return;
		}

		final IIndexBinding adapted = index.adaptBinding(declarator.getName().resolveBinding());

		final ICProject project = declarator.getTranslationUnit().getOriginatingTranslationUnit().getCProject();
		
		Set<ICPPASTFunctionDeclarator> nodes = new HashSet<>();
		
		try {
			IIndexName[] declarations = index.findNames(adapted, IIndex.FIND_DECLARATIONS_DEFINITIONS);

			for (IIndexName declaration : declarations) {
				IIndexFileLocation file = declaration.getFile().getLocation();
				ITranslationUnit tu = CoreModelUtil.findTranslationUnitForLocation(file, project);
				IASTTranslationUnit ast = cache.getASTTranslationUnit(tu);
				IASTName currentName = ast.getNodeSelector(null).findName(declaration.getNodeOffset(),
						declaration.getNodeLength());
				ICPPASTFunctionDeclarator currentNode;
				if ((currentNode = Relation.getAncestorOf(ICPPASTFunctionDeclarator.class, currentName)) != null) {
					nodes.add(currentNode);
				}
			}
		} catch (CoreException e) {

		}
		
		final Set<ICPPASTFunctionDeclarator> decls = nodes;

		hasMultipleChanges = decls.size() > 1;

		for (final ICPPASTFunctionDeclarator decl : decls) {
			final ASTRewrite rewrite = cache.getASTRewrite(decl.getTranslationUnit().getOriginatingTranslationUnit());
			final ICPPASTFunctionDeclarator replacement = decl.copy();
			replacement.setConst(true);
			rewrite.replace(decl, replacement, null);
		}

	}

}
