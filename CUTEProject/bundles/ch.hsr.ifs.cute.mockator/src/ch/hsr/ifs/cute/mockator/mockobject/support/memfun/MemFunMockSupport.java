package ch.hsr.ifs.cute.mockator.mockobject.support.memfun;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.testdouble.PolymorphismKind;


public class MemFunMockSupport extends AbstractMemFunMockSupport {

   public MemFunMockSupport(final ASTRewrite rewriter, final CppStandard cppStd, final String nameOfAllCallsVector,
                            final PolymorphismKind polymorphismKind) {
      super(rewriter, cppStd, nameOfAllCallsVector, polymorphismKind);
   }

   @Override
   public void addMockSupport(final ICPPASTFunctionDefinition function) {
      rewriter.replace(function.getBody(), createNewFunBody(function), null);
   }

   @Override
   protected void fillFunBody(final IASTCompoundStatement newBody, final ICPPASTFunctionDefinition function) {
      addCallRegistration(newBody, function);
      addAllExistingBodyStmts(newBody, function);
   }
}
