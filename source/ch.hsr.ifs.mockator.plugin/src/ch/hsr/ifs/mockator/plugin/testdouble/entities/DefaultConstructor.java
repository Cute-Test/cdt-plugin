package ch.hsr.ifs.mockator.plugin.testdouble.entities;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFunImplStrategy;
import ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.memfun.AbstractStaticPolyMissingMemFun;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionEquivalenceVerifier.ConstStrategy;
import ch.hsr.ifs.mockator.plugin.testdouble.PolymorphismKind;
import ch.hsr.ifs.mockator.plugin.testdouble.support.BaseClassCtorCallHandler;


public class DefaultConstructor extends AbstractStaticPolyMissingMemFun {

   private static final ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   private final TestDouble            testDouble;

   public DefaultConstructor(final TestDouble testDouble) {
      this.testDouble = testDouble;
   }

   @Override
   public ICPPASTFunctionDefinition createFunctionDefinition(final TestDoubleMemFunImplStrategy strategy, final CppStandard cppStd) {
      final ICPPASTFunctionDefinition defaultCtor = super.createFunctionDefinition(strategy, cppStd);
      addBaseClassCtorCallIfNecessary(defaultCtor, cppStd);
      addAdditionalCtorSupport(defaultCtor, cppStd);
      return defaultCtor;
   }

   @Override
   protected ICPPASTFunctionDeclarator createFunDecl() {
      final IASTName ctorName = createNewCtorName();
      return nodeFactory.newFunctionDeclarator(ctorName);
   }

   @Override
   protected ICPPASTDeclSpecifier createReturnType(final ICPPASTFunctionDeclarator funDecl) {
      return createCtorReturnType();
   }

   @Override
   protected IASTCompoundStatement createFunBody(final TestDoubleMemFunImplStrategy strategy, final ICPPASTFunctionDeclarator funDecl,
         final ICPPASTDeclSpecifier specifier, final CppStandard cppStd) {
      return createEmptyFunBody();
   }

   @Override
   public Collection<IASTInitializerClause> createDefaultArguments(final CppStandard cppStd, final LinkedEditModeStrategy linkedEdit) {
      return list();
   }

   private void addAdditionalCtorSupport(final ICPPASTFunctionDefinition defaultCtor, final CppStandard cppStd) {
      testDouble.addAdditionalCtorSupport(defaultCtor, cppStd);
   }

   private void addBaseClassCtorCallIfNecessary(final ICPPASTFunctionDefinition defaultCtor, final CppStandard cppStd) {
      if (testDouble.getPolymorphismKind() != PolymorphismKind.SubTypePoly) { return; }

      final BaseClassCtorCallHandler handler = new BaseClassCtorCallHandler(testDouble.getClassType());
      handler.getBaseClassInitializer(cppStd).ifPresent((candidate) -> defaultCtor.addMemberInitializer(candidate));
   }

   private IASTName createNewCtorName() {
      return nodeFactory.newName(testDouble.getName().toCharArray());
   }

   @Override
   public boolean isCallEquivalent(final ICPPASTFunctionDefinition function, final ConstStrategy strategy) {
      return false;
   }

   @Override
   public boolean isStatic() {
      return false;
   }

   @Override
   protected IASTExpression getUnderlyingExpression() {
      return null;
   }
}
