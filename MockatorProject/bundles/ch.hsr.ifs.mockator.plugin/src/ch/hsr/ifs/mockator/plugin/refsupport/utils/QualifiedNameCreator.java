package ch.hsr.ifs.mockator.plugin.refsupport.utils;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;


public class QualifiedNameCreator {

   private static final ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   private final IASTName               name;

   public QualifiedNameCreator(final IASTName name) {
      this.name = name;
   }

   public ICPPASTQualifiedName createQualifiedName() {
      final ArrayList<IASTNode> nodes = collectQualifiedNames();
      final ICPPASTQualifiedName qName = nodeFactory.newQualifiedName(null);

      for (int i = nodes.size() - 1; i >= 0; i--) {
         final IASTNode node = nodes.get(i);
         if (node instanceof IASTCompositeTypeSpecifier) {
            qName.addName(((IASTCompositeTypeSpecifier) node).getName());
         } else if (node instanceof ICPPASTNamespaceDefinition) {
            qName.addName(((ICPPASTNamespaceDefinition) node).getName());
         }
      }

      return qName;
   }

   private ArrayList<IASTNode> collectQualifiedNames() {
      final ArrayList<IASTNode> qNames = new ArrayList<>();
      IASTNode tmpNode = name;

      while (tmpNode.getParent() != null && tmpNode.getParent() != name.getTranslationUnit()) {
         tmpNode = tmpNode.getParent();

         if (tmpNode instanceof IASTCompositeTypeSpecifier) {
            qNames.add(((IASTCompositeTypeSpecifier) tmpNode).copy(CopyStyle.withLocations));
         } else if (tmpNode instanceof ICPPASTNamespaceDefinition) {
            qNames.add(((ICPPASTNamespaceDefinition) tmpNode).copy(CopyStyle.withLocations));
         }
      }
      return qNames;
   }
}
