package ch.hsr.ifs.mockator.plugin.incompleteclass.subtype;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;

import ch.hsr.ifs.iltis.core.exception.ILTISException;


import ch.hsr.ifs.mockator.plugin.incompleteclass.AbstractTestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFunImplStrategy;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionSignatureFormatter;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.DefaultArgumentCreator;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.ParameterNameFunDecorator;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.returntypes.ReturnStatementCreator;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;


class MissingMemFun extends AbstractTestDoubleMemFun implements MissingMemberFunction {

   private static ICPPNodeFactory          nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   private final ICPPASTFunctionDeclarator funDecl;
   private final IASTSimpleDeclaration     simpleDecl;

   public MissingMemFun(final IASTSimpleDeclaration simpleDecl) {
      funDecl = AstUtil.getChildOfType(simpleDecl, ICPPASTFunctionDeclarator.class);
      ILTISException.Unless.notNull(funDecl, "Not a valid function declaration");
      this.simpleDecl = simpleDecl;
   }

   @Override
   public String getFunctionSignature() {
      return new FunctionSignatureFormatter(funDecl).getFunctionSignature();
   }

   @Override
   public ICPPASTFunctionDefinition createFunctionDefinition(final TestDoubleMemFunImplStrategy strategy, final CppStandard cppStd) {
      final ICPPASTFunctionDeclarator newFunDecl = createFunDecl();
      final ICPPASTDeclSpecifier newReturnType = createReturnType();
      final IASTCompoundStatement newFunBody = createFunBody(strategy, cppStd, newFunDecl, newReturnType);
      return nodeFactory.newFunctionDefinition(newReturnType, newFunDecl, newFunBody);
   }

   private ICPPASTFunctionDeclarator createFunDecl() {
      final ICPPASTFunctionDeclarator newFunDecl = funDecl.copy();
      adjustParamNamesIfNecessary(newFunDecl);
      newFunDecl.setPureVirtual(false);
      return newFunDecl;
   }

   private ICPPASTDeclSpecifier createReturnType() {
      final ICPPASTDeclSpecifier returnType = (ICPPASTDeclSpecifier) simpleDecl.getDeclSpecifier();
      final ICPPASTDeclSpecifier newReturnType = returnType.copy();
      newReturnType.setVirtual(false);
      return newReturnType;
   }

   private IASTCompoundStatement createFunBody(final TestDoubleMemFunImplStrategy strategy, final CppStandard cppStd,
         final ICPPASTFunctionDeclarator newFunDecl, final ICPPASTDeclSpecifier newReturnType) {
      final IASTCompoundStatement newFunBody = nodeFactory.newCompoundStatement();
      strategy.addCallVectorRegistration(newFunBody, newFunDecl, isStatic());
      final ReturnStatementCreator creator = new ReturnStatementCreator(cppStd, getClassName());
      final IASTReturnStatement returnStatement = creator.createReturnStatement(funDecl, newReturnType);
      newFunBody.addStatement(returnStatement);
      return newFunBody;
   }

   private static void adjustParamNamesIfNecessary(final ICPPASTFunctionDeclarator newFunDecl) {
      final ParameterNameFunDecorator funDecorator = new ParameterNameFunDecorator(newFunDecl);
      funDecorator.adjustParamNamesIfNecessary();
   }

   private String getClassName() {
      final ICPPASTCompositeTypeSpecifier klass = AstUtil.getAncestorOfType(simpleDecl, ICPPASTCompositeTypeSpecifier.class);
      return klass.getName().toString();
   }

   @Override
   public Collection<IASTInitializerClause> createDefaultArguments(final CppStandard cppStd, final LinkedEditModeStrategy strategy) {
      final DefaultArgumentCreator creator = new DefaultArgumentCreator(strategy, cppStd);
      return creator.createDefaultArguments(list(funDecl.getParameters()));
   }

   @Override
   public boolean isStatic() {
      return false;
   }
}
