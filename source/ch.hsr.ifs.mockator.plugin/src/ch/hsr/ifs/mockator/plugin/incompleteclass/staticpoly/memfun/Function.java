package ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.memfun;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFunImplStrategy;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionEquivalenceVerifier;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionEquivalenceVerifier.ConstStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.ParameterToFunctionAdder;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.returntypes.ReturnStatementCreator;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.returntypes.ReturnTypeDeducer;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;


class Function extends AbstractStaticPolyMissingMemFun {

   private final ICPPASTFunctionCallExpression funCall;
   private final boolean                       isStatic;
   private final IType                         injectedType;
   private final String                        memberClassName;

   public Function(final ICPPASTFunctionCallExpression funCall, final boolean isStatic, final IType injectedType, final String memberClassName) {
      this.funCall = funCall;
      this.isStatic = isStatic;
      this.injectedType = injectedType;
      this.memberClassName = memberClassName;
   }

   @Override
   @SuppressWarnings("restriction")
   protected ICPPASTFunctionDeclarator createFunDecl() {
      final IASTName funName = nodeFactory.newName(AstUtil.getName(funCall).toCharArray());
      final ICPPASTFunctionDeclarator funDecl = nodeFactory.newFunctionDeclarator(funName);
      funDecl.setConst(!isStatic);
      new ParameterToFunctionAdder(funDecl).addParametersFromFunCall(funCall);
      return funDecl;
   }

   @Override
   protected ICPPASTDeclSpecifier createReturnType(final ICPPASTFunctionDeclarator funDecl) {
      final ReturnTypeDeducer deducer = new ReturnTypeDeducer(funDecl, injectedType, memberClassName);
      final ICPPASTDeclSpecifier returnType = deducer.determineReturnType(funCall);

      if (isStatic) {
         returnType.setStorageClass(IASTDeclSpecifier.sc_static);
      }

      return returnType;
   }

   @Override
   protected IASTCompoundStatement createFunBody(final TestDoubleMemFunImplStrategy strategy, final ICPPASTFunctionDeclarator funDecl,
         final ICPPASTDeclSpecifier specifier, final CppStandard cppStd) {
      final IASTCompoundStatement newFunBody = createEmptyFunBody();
      strategy.addCallVectorRegistration(newFunBody, funDecl, isStatic);
      final ReturnStatementCreator creator = new ReturnStatementCreator(cppStd, memberClassName);
      final IASTReturnStatement returnStatement = creator.createReturnStatement(funDecl, specifier);
      newFunBody.addStatement(returnStatement);
      return newFunBody;
   }

   @Override
   protected IASTExpression getUnderlyingExpression() {
      return funCall;
   }

   @Override
   public boolean isCallEquivalent(final ICPPASTFunctionDefinition function, final ConstStrategy strategy) {
      final FunctionEquivalenceVerifier checker = new FunctionEquivalenceVerifier((ICPPASTFunctionDeclarator) function.getDeclarator());
      return checker.isEquivalent(funCall, strategy);
   }

   @Override
   public boolean isStatic() {
      return isStatic;
   }
}
