package ch.hsr.ifs.mockator.plugin.mockobject.support.memfun;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.testdouble.PolymorphismKind;


public class CtorMockSupport extends AbstractMemFunMockSupport {

   public CtorMockSupport(ASTRewrite rewriter, CppStandard cppStd, String nameOfAllCallsVector, PolymorphismKind polymorphismKind) {
      super(rewriter, cppStd, nameOfAllCallsVector, polymorphismKind);
   }

   @Override
   public void addMockSupport(ICPPASTFunctionDefinition function) {
      ICPPASTFunctionDefinition newFun = function.copy();
      addCtorInitializer(newFun);
      newFun.setBody(createNewFunBody(function));
      rewriter.replace(function, newFun, null);
   }

   private void addCtorInitializer(ICPPASTFunctionDefinition ctor) {
      new MockIdInitializerAdder(callsVectorName, cppStd).apply(ctor);
   }

   @Override
   protected void fillFunBody(IASTCompoundStatement newBody, ICPPASTFunctionDefinition function) {
      addAllExistingBodyStmts(newBody, function);

      if (!isSubTypePoly()) {
         addCallRegistration(newBody, function);
      }
   }
}
