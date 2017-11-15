package ch.hsr.ifs.mockator.plugin.refsupport.finder;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;


public class ClassInSelectionFinder {

   private final ITextSelection   selection;
   private final IASTNodeSelector nodeSelector;

   public ClassInSelectionFinder(final ITextSelection selection, final IASTTranslationUnit ast) {
      this.selection = selection;
      nodeSelector = ast.getNodeSelector(null);
   }

   public Optional<ICPPASTCompositeTypeSpecifier> getClassInSelection() {
      ICPPASTCompositeTypeSpecifier klass = getFirstContainingNodeSelection();
      if (klass == null) {
         klass = getFirstEnclosingNodeSelection();
      }
      return Optional.ofNullable(klass);
   }

   private ICPPASTCompositeTypeSpecifier getFirstEnclosingNodeSelection() {
      return getContainingClass(nodeSelector.findEnclosingNode(selection.getOffset(), selection.getLength()));
   }

   private ICPPASTCompositeTypeSpecifier getFirstContainingNodeSelection() {
      return getContainingClass(nodeSelector.findFirstContainedNode(selection.getOffset(), selection.getLength()));
   }

   private static ICPPASTCompositeTypeSpecifier getContainingClass(final IASTNode node) {
      return ASTUtil.getAncestorOfType(node, ICPPASTCompositeTypeSpecifier.class);
   }
}
