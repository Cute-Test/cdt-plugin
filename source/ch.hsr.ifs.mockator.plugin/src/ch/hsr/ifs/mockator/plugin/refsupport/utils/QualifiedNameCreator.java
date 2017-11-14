package ch.hsr.ifs.mockator.plugin.refsupport.utils;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.base.collections.MyStack;


public class QualifiedNameCreator {

   private static final ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   private final IASTName              name;

   public QualifiedNameCreator(final IASTName name) {
      this.name = name;
   }

   public ICPPASTQualifiedName createQualifiedName() {
      final MyStack<IASTNode> nodes = collectQualifiedNames();
      final ICPPASTQualifiedName qName = nodeFactory.newQualifiedName(null);

      nodes.stream().forEach((node) -> {
         if (node instanceof IASTCompositeTypeSpecifier) {
            qName.addName(((IASTCompositeTypeSpecifier) node).getName());
         } else if (node instanceof ICPPASTNamespaceDefinition) {
            qName.addName(((ICPPASTNamespaceDefinition) node).getName());
         } else if (node instanceof ICPPASTTemplateId) {
            qName.addName((ICPPASTTemplateId) node);
         }
      });

      return qName;
   }

   private MyStack<IASTNode> collectQualifiedNames() {
      final MyStack<IASTNode> qNames = new MyStack<>();
      IASTNode tmpNode = name;

      while (tmpNode.getParent() != null && tmpNode.getParent() != name.getTranslationUnit()) {
         tmpNode = tmpNode.getParent();

         if (tmpNode instanceof IASTCompositeTypeSpecifier) {
            qNames.push(((IASTCompositeTypeSpecifier) tmpNode).copy(CopyStyle.withLocations));
         } else if (tmpNode instanceof ICPPASTNamespaceDefinition) {
            qNames.push(((ICPPASTNamespaceDefinition) tmpNode).copy(CopyStyle.withLocations));
         }
      }
      return qNames;
   }
}
