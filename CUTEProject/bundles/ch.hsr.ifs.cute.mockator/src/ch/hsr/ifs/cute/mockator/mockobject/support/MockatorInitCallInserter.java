package ch.hsr.ifs.cute.mockator.mockobject.support;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.mockator.MockatorConstants;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;
import ch.hsr.ifs.cute.mockator.refsupport.finder.MacroFinderVisitor;


// Inserts the preprocessor statement for initializing Mockator functionality:
// INIT_MOCKATOR();
class MockatorInitCallInserter {

   private final ICPPASTCompositeTypeSpecifier mockObject;
   private final IASTNode                      parent;

   public MockatorInitCallInserter(final ICPPASTCompositeTypeSpecifier mockObject, final IASTNode parent) {
      this.mockObject = mockObject;
      this.parent = parent;
   }

   public void insertWith(final ASTRewrite rewriter) {
      if (!hasMockatorInitCall()) {
         final IASTNode initCall = createMockatorInitCall();
         insertMockatorInitCall(rewriter, initCall);
      }
   }

   private IASTNode createMockatorInitCall() {
      return new MockatorInitCallCreator(parent).createMockatorInitCall();
   }

   private void insertMockatorInitCall(final ASTRewrite rewriter, final IASTNode initCall) {
      rewriter.insertBefore(parent, getInsertionPoint(), initCall, null);
   }

   private IASTNode getInsertionPoint() {
      final ICPPASTFunctionDefinition function = getFunctionParent();

      if (function != null) {
         final IASTNode[] children = function.getBody().getChildren();
         return children.length > 0 ? children[0] : null;
      }
      return CPPVisitor.findAncestorWithType(mockObject, IASTSimpleDeclaration.class).orElse(null);
   }

   private ICPPASTFunctionDefinition getFunctionParent() {
      return CPPVisitor.findAncestorWithType(mockObject, ICPPASTFunctionDefinition.class).orElse(null);
   }

   private boolean hasMockatorInitCall() {
      final MacroFinderVisitor macroFinder = new MacroFinderVisitor(MockatorConstants.INIT_MOCKATOR);
      parent.accept(macroFinder);
      return !macroFinder.getMatchingMacroExpansions().isEmpty();
   }
}
