package ch.hsr.ifs.mockator.plugin.refsupport.utils;

import java.util.LinkedList;

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
      final LinkedList<ICPPASTNamespaceDefinition> namespaces = getOriginNamespaces();

      if (namespaces.isEmpty()) { return decl; }

      final ICPPASTNamespaceDefinition topNs = namespaces.pop();
      ICPPASTNamespaceDefinition parentNs = topNs;
      ICPPASTNamespaceDefinition currentNs = null;

      while (!namespaces.isEmpty()) {
         currentNs = namespaces.pop();
         parentNs.addDeclaration(currentNs);
         parentNs = currentNs;
      }

      parentNs.addDeclaration(decl);
      return topNs;
   }

   private LinkedList<ICPPASTNamespaceDefinition> getOriginNamespaces() {
      final LinkedList<ICPPASTNamespaceDefinition> namespaces = new LinkedList<>();
      //      final Stack<ICPPASTNamespaceDefinition> namespaces = new Stack<>();

      for (IASTNode currNode = origin; currNode != null; currNode = currNode.getParent()) {
         if (currNode instanceof ICPPASTNamespaceDefinition) {
            namespaces.push(copyNamespace((ICPPASTNamespaceDefinition) currNode));
         }
      }

      return namespaces;
   }

   private static ICPPASTNamespaceDefinition copyNamespace(final ICPPASTNamespaceDefinition ns) {
      return nodeFactory.newNamespaceDefinition(ns.getName().copy());
   }
}
