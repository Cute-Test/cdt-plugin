package ch.hsr.ifs.mockator.plugin.testdouble;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;


public class TestDoubleParentFinder {

   private final ICPPASTCompositeTypeSpecifier testDouble;

   public TestDoubleParentFinder(ICPPASTCompositeTypeSpecifier testDouble) {
      this.testDouble = testDouble;
   }

   public IASTNode getParentOfTestDouble() {
      IASTNode currentNode = testDouble;

      while (currentNode != null) {
         if (currentNode instanceof ICPPASTFunctionDefinition) return ((ICPPASTFunctionDefinition) currentNode).getBody();

         if (currentNode instanceof ICPPASTNamespaceDefinition) return currentNode;

         currentNode = currentNode.getParent();
      }

      return null;
   }
}
