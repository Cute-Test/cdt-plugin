package ch.hsr.ifs.mockator.plugin.mockobject.support.memfun;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.testdouble.PolymorphismKind;


public class MemFunMockSupport extends AbstractMemFunMockSupport {

   public MemFunMockSupport(ASTRewrite rewriter, CppStandard cppStd, String nameOfAllCallsVector, PolymorphismKind polymorphismKind) {
      super(rewriter, cppStd, nameOfAllCallsVector, polymorphismKind);
   }

   @Override
   public void addMockSupport(ICPPASTFunctionDefinition function) {
      rewriter.replace(function.getBody(), createNewFunBody(function), null);
   }

   @Override
   protected void fillFunBody(IASTCompoundStatement newBody, ICPPASTFunctionDefinition function) {
      addCallRegistration(newBody, function);
      addAllExistingBodyStmts(newBody, function);
   }
}
