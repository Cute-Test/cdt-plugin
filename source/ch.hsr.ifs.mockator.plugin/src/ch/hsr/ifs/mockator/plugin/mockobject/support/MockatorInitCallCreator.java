package ch.hsr.ifs.mockator.plugin.mockobject.support;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.INIT_MOCKATOR;
import static ch.hsr.ifs.mockator.plugin.MockatorConstants.L_PARENTHESIS;
import static ch.hsr.ifs.mockator.plugin.MockatorConstants.R_PARENTHESIS;

import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

@SuppressWarnings("restriction")
public class MockatorInitCallCreator {
  private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();
  private final IASTNode parent;

  public MockatorInitCallCreator(IASTNode parent) {
    this.parent = parent;
  }

  public IASTNode createMockatorInitCall() {
    if (isFunctionParent())
      return createMockatorInitForFunction();
    else
      return createMockatorInitForNamespace();
  }

  private boolean isFunctionParent() {
    return AstUtil.getAncestorOfType(parent, ICPPASTFunctionDefinition.class) != null;
  }

  private static IASTNode createMockatorInitForNamespace() {
    IASTName initMockator =
        nodeFactory.newName((INIT_MOCKATOR + L_PARENTHESIS + R_PARENTHESIS).toCharArray());
    return nodeFactory.newSimpleDeclaration(nodeFactory.newTypedefNameSpecifier(initMockator));
  }

  private static IASTExpressionStatement createMockatorInitForFunction() {
    IASTIdExpression initMockator =
        nodeFactory.newIdExpression(nodeFactory.newName(INIT_MOCKATOR.toCharArray()));
    IASTInitializerClause[] noArgs = new IASTInitializerClause[] {};
    IASTFunctionCallExpression initMockatorFunCall =
        nodeFactory.newFunctionCallExpression(initMockator, noArgs);
    return nodeFactory.newExpressionStatement(initMockatorFunCall);
  }
}
