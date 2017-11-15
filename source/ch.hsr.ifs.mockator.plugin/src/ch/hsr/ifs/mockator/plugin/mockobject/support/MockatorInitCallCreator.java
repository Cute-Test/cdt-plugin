package ch.hsr.ifs.mockator.plugin.mockobject.support;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.INIT_MOCKATOR;
import static ch.hsr.ifs.mockator.plugin.MockatorConstants.L_PARENTHESIS;
import static ch.hsr.ifs.mockator.plugin.MockatorConstants.R_PARENTHESIS;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;

import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;


public class MockatorInitCallCreator {

   private static final ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   private final IASTNode              parent;

   public MockatorInitCallCreator(final IASTNode parent) {
      this.parent = parent;
   }

   public IASTNode createMockatorInitCall() {
      if (isFunctionParent()) {
         return createMockatorInitForFunction();
      } else {
         return createMockatorInitForNamespace();
      }
   }

   private boolean isFunctionParent() {
      return ASTUtil.getAncestorOfType(parent, ICPPASTFunctionDefinition.class) != null;
   }

   private static IASTNode createMockatorInitForNamespace() {
      final IASTName initMockator = nodeFactory.newName((INIT_MOCKATOR + L_PARENTHESIS + R_PARENTHESIS).toCharArray());
      return nodeFactory.newSimpleDeclaration(nodeFactory.newTypedefNameSpecifier(initMockator));
   }

   private static IASTExpressionStatement createMockatorInitForFunction() {
      final IASTIdExpression initMockator = nodeFactory.newIdExpression(nodeFactory.newName(INIT_MOCKATOR.toCharArray()));
      final IASTInitializerClause[] noArgs = new IASTInitializerClause[] {};
      final IASTFunctionCallExpression initMockatorFunCall = nodeFactory.newFunctionCallExpression(initMockator, noArgs);
      return nodeFactory.newExpressionStatement(initMockatorFunCall);
   }
}
