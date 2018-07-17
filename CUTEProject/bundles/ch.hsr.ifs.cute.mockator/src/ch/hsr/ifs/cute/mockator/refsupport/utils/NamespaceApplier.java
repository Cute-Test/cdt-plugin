package ch.hsr.ifs.cute.mockator.refsupport.utils;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;


public class NamespaceApplier {

   private static final ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   private final IASTNode               origin;

   public NamespaceApplier(final IASTNode origin) {
      this.origin = origin;
   }

   public IASTNode packInSameNamespaces(final IASTSimpleDeclaration decl) {
      final ArrayList<ICPPASTNamespaceDefinition> namespaces = getOriginNamespaces();

      if (namespaces.isEmpty()) return decl;

      final ICPPASTNamespaceDefinition topNs = namespaces.get(namespaces.size() - 1);
      ICPPASTNamespaceDefinition parentNs = topNs;
      ICPPASTNamespaceDefinition currentNs = null;

      for (int i = namespaces.size() - 2; i >= 0; i--) {
         currentNs = namespaces.get(i);
         parentNs.addDeclaration(currentNs);
         parentNs = currentNs;
      }

      parentNs.addDeclaration(decl);
      return topNs;
   }

   /**
    * @return An ArrayList where the first element is the innermost namespace and the last one the outermost namespace
    */
   private ArrayList<ICPPASTNamespaceDefinition> getOriginNamespaces() {
      final ArrayList<ICPPASTNamespaceDefinition> namespaces = new ArrayList<>();

      for (IASTNode currNode = origin; currNode != null; currNode = currNode.getParent()) {
         if (currNode instanceof ICPPASTNamespaceDefinition) {
            namespaces.add(copyNamespace((ICPPASTNamespaceDefinition) currNode));
         }
      }

      return namespaces;
   }

   private static ICPPASTNamespaceDefinition copyNamespace(final ICPPASTNamespaceDefinition ns) {
      return nodeFactory.newNamespaceDefinition(ns.getName().copy());
   }
}
