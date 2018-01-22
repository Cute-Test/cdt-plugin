package ch.hsr.ifs.mockator.plugin.refsupport.functions.params;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;


public class DefaultArgumentCreator {

   private static final ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   private final CppStandard            cppStd;
   private final LinkedEditModeStrategy linkedEdit;

   public DefaultArgumentCreator(final LinkedEditModeStrategy linkedEdit, final CppStandard cppStd) {
      this.linkedEdit = linkedEdit;
      this.cppStd = cppStd;
   }

   public Collection<IASTInitializerClause> createDefaultArguments(final Collection<ICPPASTParameterDeclaration> funParams) {
      final List<IASTInitializerClause> defaultArguments = new ArrayList<>();

      if (linkedEdit != LinkedEditModeStrategy.ChooseArguments) {
         return defaultArguments;
      }

      addDefaultArgs(funParams, defaultArguments);
      return defaultArguments;
   }

   private void addDefaultArgs(final Collection<ICPPASTParameterDeclaration> funParams, final List<IASTInitializerClause> defaultArgs) {
      for (final ICPPASTParameterDeclaration p : funParams) {
         final ICPPASTDeclSpecifier returnDeclSpec = (ICPPASTDeclSpecifier) p.getDeclSpecifier().copy();
         returnDeclSpec.setStorageClass(IASTDeclSpecifier.sc_unspecified);
         returnDeclSpec.setConst(false);
         removeUnsignedIfNecessary(returnDeclSpec);
         final IASTInitializer emptyInitializer = getEmptyInitializer(cppStd);
         final ICPPASTSimpleTypeConstructorExpression returnType = nodeFactory.newSimpleTypeConstructorExpression(returnDeclSpec, emptyInitializer);
         defaultArgs.add(returnType);
      }
   }

   private static void removeUnsignedIfNecessary(final ICPPASTDeclSpecifier declSpec) {
      // unsigned int{} is not allowed in C++
      if (declSpec instanceof ICPPASTSimpleDeclSpecifier && ((ICPPASTSimpleDeclSpecifier) declSpec).isUnsigned()) {
         ((ICPPASTSimpleDeclSpecifier) declSpec).setUnsigned(false);
      }
   }

   private static IASTInitializer getEmptyInitializer(final CppStandard cppStd) {
      return cppStd.getEmptyInitializer();
   }
}
