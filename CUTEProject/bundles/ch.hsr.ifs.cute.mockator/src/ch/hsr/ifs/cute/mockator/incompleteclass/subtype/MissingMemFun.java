package ch.hsr.ifs.cute.mockator.incompleteclass.subtype;

import static ch.hsr.ifs.iltis.core.core.collections.CollectionUtil.list;

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

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.cute.mockator.incompleteclass.AbstractTestDoubleMemFun;
import ch.hsr.ifs.cute.mockator.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.cute.mockator.incompleteclass.TestDoubleMemFunImplStrategy;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.cute.mockator.refsupport.functions.FunctionSignatureFormatter;
import ch.hsr.ifs.cute.mockator.refsupport.functions.params.DefaultArgumentCreator;
import ch.hsr.ifs.cute.mockator.refsupport.functions.params.ParameterNameFunDecorator;
import ch.hsr.ifs.cute.mockator.refsupport.functions.returntypes.ReturnStatementCreator;


class MissingMemFun extends AbstractTestDoubleMemFun implements MissingMemberFunction {

   private static ICPPNodeFactory          nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   private final ICPPASTFunctionDeclarator funDecl;
   private final IASTSimpleDeclaration     simpleDecl;

   public MissingMemFun(final IASTSimpleDeclaration simpleDecl) {
      funDecl = CPPVisitor.findChildWithType(simpleDecl, ICPPASTFunctionDeclarator.class).orElse(null);
      ILTISException.Unless.notNull("Not a valid function declaration", funDecl);
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
      final ICPPASTCompositeTypeSpecifier clazz = CPPVisitor.findAncestorWithType(simpleDecl, ICPPASTCompositeTypeSpecifier.class).orElse(null);
      return clazz.getName().toString();
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
