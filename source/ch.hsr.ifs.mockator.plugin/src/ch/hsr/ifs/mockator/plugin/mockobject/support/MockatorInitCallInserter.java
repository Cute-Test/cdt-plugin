package ch.hsr.ifs.mockator.plugin.mockobject.support;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.MacroFinderVisitor;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

// Inserts the preprocessor statement for initializing Mockator functionality:
// INIT_MOCKATOR();
class MockatorInitCallInserter {
  private final ICPPASTCompositeTypeSpecifier mockObject;
  private final IASTNode parent;

  public MockatorInitCallInserter(ICPPASTCompositeTypeSpecifier mockObject, IASTNode parent) {
    this.mockObject = mockObject;
    this.parent = parent;
  }

  public void insertWith(ASTRewrite rewriter) {
    if (!hasMockatorInitCall()) {
      IASTNode initCall = createMockatorInitCall();
      insertMockatorInitCall(rewriter, initCall);
    }
  }

  private IASTNode createMockatorInitCall() {
    return new MockatorInitCallCreator(parent).createMockatorInitCall();
  }

  private void insertMockatorInitCall(ASTRewrite rewriter, IASTNode initCall) {
    rewriter.insertBefore(parent, getInsertionPoint(), initCall, null);
  }

  private IASTNode getInsertionPoint() {
    ICPPASTFunctionDefinition function = getFunctionParent();

    if (function != null) {
      IASTNode[] children = function.getBody().getChildren();
      return children.length > 0 ? children[0] : null;
    }
    return AstUtil.getAncestorOfType(mockObject, IASTSimpleDeclaration.class);
  }

  private ICPPASTFunctionDefinition getFunctionParent() {
    return AstUtil.getAncestorOfType(mockObject, ICPPASTFunctionDefinition.class);
  }

  private boolean hasMockatorInitCall() {
    MacroFinderVisitor macroFinder = new MacroFinderVisitor(MockatorConstants.INIT_MOCKATOR);
    parent.accept(macroFinder);
    return !macroFinder.getMatchingMacroExpansions().isEmpty();
  }
}
