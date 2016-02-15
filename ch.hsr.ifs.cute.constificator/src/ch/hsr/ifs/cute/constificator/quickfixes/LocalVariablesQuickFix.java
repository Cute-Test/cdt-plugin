package ch.hsr.ifs.cute.constificator.quickfixes;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;

import ch.hsr.ifs.cute.constificator.core.asttools.ASTRewriteCache;

public class LocalVariablesQuickFix extends QuickFix {
	@Override
	public String getLabel() {
		return "Add const-qualification";
	}

	@Override
	protected void handleNode(IASTNode node, IIndex index, ASTRewriteCache cache) {
		IASTNode replacementNode = node.copy();

		if (node instanceof ICPPASTDeclSpecifier) {
			((ICPPASTDeclSpecifier) replacementNode).setConst(true);
		} else if (node instanceof IASTPointer) {
			((IASTPointer) replacementNode).setConst(true);
		} else if (node instanceof IASTName) {
			while(!(node instanceof ICPPASTDeclSpecifier) && node != null){
				node = node.getParent();
			}
			if(node != null) {
				replacementNode = node.copy();
				((ICPPASTDeclSpecifier)replacementNode).setConst(true);
			} else {
				return;
			}
		} else {
			return;
		}

		ASTRewrite rewrite = cache.getASTRewrite(node.getTranslationUnit().getOriginatingTranslationUnit());
		rewrite.replace(node, replacementNode, null);
		
	}

}
