//package ch.hsr.ifs.mockator.plugin.refsupport.finder;
//
//import java.util.Optional;
//
//TODO remove
//import org.eclipse.cdt.core.dom.ast.IASTNode;
//import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
//import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
//import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
//import org.eclipse.jface.text.ITextSelection;
//
//import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;
//
//
//public class ClassInSelectionFinder {
//
//   private final Optional<ITextSelection> selection;
//   private final IASTNodeSelector         nodeSelector;
//
//   public ClassInSelectionFinder(final Optional<ITextSelection> selection, final IASTTranslationUnit ast) {
//      this.selection = selection;
//      nodeSelector = ast.getNodeSelector(null);
//   }
//
//   public Optional<ICPPASTCompositeTypeSpecifier> getClassInSelection() {
//      ICPPASTCompositeTypeSpecifier clazz = getFirstContainingNodeSelection();
//      if (clazz == null) {
//         clazz = getFirstEnclosingNodeSelection();
//      }
//      return Optional.ofNullable(clazz);
//   }
//
//   private ICPPASTCompositeTypeSpecifier getFirstEnclosingNodeSelection() {
//      return getContainingClass(nodeSelector.findEnclosingNode(selection.map(ITextSelection::getOffset).orElse(-1), selection.map(
//            ITextSelection::getLength).orElse(0)));
//   }
//
//   private ICPPASTCompositeTypeSpecifier getFirstContainingNodeSelection() {
//      return getContainingClass(nodeSelector.findFirstContainedNode(selection.map(ITextSelection::getOffset).orElse(-1), selection.map(
//            ITextSelection::getLength).orElse(0)));
//   }
//
//   private static ICPPASTCompositeTypeSpecifier getContainingClass(final IASTNode node) {
//      return ASTUtil.getAncestorOfType(node, ICPPASTCompositeTypeSpecifier.class);
//   }
//}
