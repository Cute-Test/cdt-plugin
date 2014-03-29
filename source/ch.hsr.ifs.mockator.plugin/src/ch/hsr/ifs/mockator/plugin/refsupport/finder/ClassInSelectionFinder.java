package ch.hsr.ifs.mockator.plugin.refsupport.finder;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

public class ClassInSelectionFinder {
  private final ITextSelection selection;
  private final IASTNodeSelector nodeSelector;

  public ClassInSelectionFinder(ITextSelection selection, IASTTranslationUnit ast) {
    this.selection = selection;
    nodeSelector = ast.getNodeSelector(null);
  }

  public Maybe<ICPPASTCompositeTypeSpecifier> getClassInSelection() {
    ICPPASTCompositeTypeSpecifier klass = getFirstContainingNodeSelection();
    if (klass == null) {
      klass = getFirstEnclosingNodeSelection();
    }
    return maybe(klass);
  }

  private ICPPASTCompositeTypeSpecifier getFirstEnclosingNodeSelection() {
    return getContainingClass(nodeSelector.findEnclosingNode(selection.getOffset(),
        selection.getLength()));
  }

  private ICPPASTCompositeTypeSpecifier getFirstContainingNodeSelection() {
    return getContainingClass(nodeSelector.findFirstContainedNode(selection.getOffset(),
        selection.getLength()));
  }

  private static ICPPASTCompositeTypeSpecifier getContainingClass(IASTNode node) {
    return AstUtil.getAncestorOfType(node, ICPPASTCompositeTypeSpecifier.class);
  }
}
