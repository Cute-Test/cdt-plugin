package ch.hsr.ifs.mockator.plugin.incompleteclass.subtype;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
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


@SuppressWarnings("restriction")
class MissingMemFun extends AbstractTestDoubleMemFun implements MissingMemberFunction {

   private static CPPNodeFactory           nodeFactory = CPPNodeFactory.getDefault();
   private final ICPPASTFunctionDeclarator funDecl;
   private final IASTSimpleDeclaration     simpleDecl;

   public MissingMemFun(IASTSimpleDeclaration simpleDecl) {
      funDecl = AstUtil.getChildOfType(simpleDecl, ICPPASTFunctionDeclarator.class);
      Assert.notNull(funDecl, "Not a valid function declaration");
      this.simpleDecl = simpleDecl;
   }

   @Override
   public String getFunctionSignature() {
      return new FunctionSignatureFormatter(funDecl).getFunctionSignature();
   }

   @Override
   public ICPPASTFunctionDefinition createFunctionDefinition(TestDoubleMemFunImplStrategy strategy, CppStandard cppStd) {
      ICPPASTFunctionDeclarator newFunDecl = createFunDecl();
      ICPPASTDeclSpecifier newReturnType = createReturnType();
      IASTCompoundStatement newFunBody = createFunBody(strategy, cppStd, newFunDecl, newReturnType);
      return nodeFactory.newFunctionDefinition(newReturnType, newFunDecl, newFunBody);
   }

   private ICPPASTFunctionDeclarator createFunDecl() {
      ICPPASTFunctionDeclarator newFunDecl = funDecl.copy();
      adjustParamNamesIfNecessary(newFunDecl);
      newFunDecl.setPureVirtual(false);
      return newFunDecl;
   }

   private ICPPASTDeclSpecifier createReturnType() {
      ICPPASTDeclSpecifier returnType = (ICPPASTDeclSpecifier) simpleDecl.getDeclSpecifier();
      ICPPASTDeclSpecifier newReturnType = returnType.copy();
      newReturnType.setVirtual(false);
      return newReturnType;
   }

   private IASTCompoundStatement createFunBody(TestDoubleMemFunImplStrategy strategy, CppStandard cppStd, ICPPASTFunctionDeclarator newFunDecl,
         ICPPASTDeclSpecifier newReturnType) {
      IASTCompoundStatement newFunBody = nodeFactory.newCompoundStatement();
      strategy.addCallVectorRegistration(newFunBody, newFunDecl, isStatic());
      ReturnStatementCreator creator = new ReturnStatementCreator(cppStd, getClassName());
      IASTReturnStatement returnStatement = creator.createReturnStatement(funDecl, newReturnType);
      newFunBody.addStatement(returnStatement);
      return newFunBody;
   }

   private static void adjustParamNamesIfNecessary(ICPPASTFunctionDeclarator newFunDecl) {
      ParameterNameFunDecorator funDecorator = new ParameterNameFunDecorator(newFunDecl);
      funDecorator.adjustParamNamesIfNecessary();
   }

   private String getClassName() {
      ICPPASTCompositeTypeSpecifier klass = AstUtil.getAncestorOfType(simpleDecl, ICPPASTCompositeTypeSpecifier.class);
      return klass.getName().toString();
   }

   @Override
   public Collection<IASTInitializerClause> createDefaultArguments(CppStandard cppStd, LinkedEditModeStrategy strategy) {
      DefaultArgumentCreator creator = new DefaultArgumentCreator(strategy, cppStd);
      return creator.createDefaultArguments(list(funDecl.getParameters()));
   }

   @Override
   public boolean isStatic() {
      return false;
   }
}
