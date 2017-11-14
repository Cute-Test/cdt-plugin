package ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.memfun;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFunImplStrategy;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionEquivalenceVerifier;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionEquivalenceVerifier.ConstStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.ParameterToFunctionAdder;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;


class Constructor extends AbstractStaticPolyMissingMemFun {

   private final ICPPASTFunctionCallExpression funCall;

   public Constructor(final ICPPASTFunctionCallExpression funCall) {
      this.funCall = funCall;
   }

   @Override
   protected ICPPASTFunctionDeclarator createFunDecl() {
      final IASTName funName = nodeFactory.newName(AstUtil.getName(funCall).toCharArray());
      final ICPPASTFunctionDeclarator funDecl = nodeFactory.newFunctionDeclarator(funName);
      new ParameterToFunctionAdder(funDecl).addParametersFromFunCall(funCall);
      return funDecl;
   }

   @Override
   protected ICPPASTDeclSpecifier createReturnType(final ICPPASTFunctionDeclarator funDecl) {
      return createCtorReturnType();
   }

   @Override
   public ICPPASTFunctionDefinition createFunctionDefinition(final TestDoubleMemFunImplStrategy strategy, final CppStandard cppStd) {
      final ICPPASTFunctionDefinition function = super.createFunctionDefinition(strategy, cppStd);
      strategy.addCtorInitializer(function);
      return function;
   }

   @Override
   protected IASTCompoundStatement createFunBody(final TestDoubleMemFunImplStrategy strategy, final ICPPASTFunctionDeclarator funDecl,
         final ICPPASTDeclSpecifier specifier, final CppStandard cppStd) {
      final IASTCompoundStatement newFunBody = createEmptyFunBody();
      strategy.addCallVectorRegistration(newFunBody, funDecl, false);
      return newFunBody;
   }

   public boolean isDefaultConstructor() {
      return funCall.getArguments().length == 0;
   }

   @Override
   public boolean isCallEquivalent(final ICPPASTFunctionDefinition function, final ConstStrategy strategy) {
      final FunctionEquivalenceVerifier checker = new FunctionEquivalenceVerifier((ICPPASTFunctionDeclarator) function.getDeclarator());
      return checker.isEquivalent(funCall, strategy);
   }

   @Override
   protected IASTExpression getUnderlyingExpression() {
      return funCall;
   }

   @Override
   public boolean isStatic() {
      return false;
   }
}
