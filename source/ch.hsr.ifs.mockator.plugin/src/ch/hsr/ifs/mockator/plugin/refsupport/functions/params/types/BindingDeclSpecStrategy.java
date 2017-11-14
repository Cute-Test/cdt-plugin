package ch.hsr.ifs.mockator.plugin.refsupport.functions.params.types;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.iltis.core.exception.ILTISException;

import ch.hsr.ifs.mockator.plugin.refsupport.utils.TypedefHelper;


class BindingDeclSpecStrategy implements DeclSpecGeneratorStrategy {

   private static final ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();

   @Override
   public ICPPASTDeclSpecifier createDeclSpec(final IType type) {
      final String shortestType = getShortestType(type);
      final IASTName newName = nodeFactory.newName(shortestType.toCharArray());
      return nodeFactory.newTypedefNameSpecifier(newName);
   }

   private static String getShortestType(final IType type) {
      try {
         return new TypedefHelper(type).findShortestType();
      }
      catch (final CoreException e) {
         throw new ILTISException(e).rethrowUnchecked();
      }
   }
}
