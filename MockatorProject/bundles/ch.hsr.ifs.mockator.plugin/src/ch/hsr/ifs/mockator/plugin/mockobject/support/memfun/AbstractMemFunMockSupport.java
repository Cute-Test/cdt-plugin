package ch.hsr.ifs.mockator.plugin.mockobject.support.memfun;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;
import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;

import ch.hsr.ifs.mockator.plugin.mockobject.registrations.MemberFunCallRegistrationAdder;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.testdouble.MemFunMockSupportAdder;
import ch.hsr.ifs.mockator.plugin.testdouble.PolymorphismKind;


abstract class AbstractMemFunMockSupport implements MemFunMockSupportAdder {

   private static final ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   protected final ASTRewrite           rewriter;
   protected final CppStandard          cppStd;
   protected final String               callsVectorName;
   private final PolymorphismKind       polyKind;

   public AbstractMemFunMockSupport(final ASTRewrite rewriter, final CppStandard cppStd, final String nameOfAllCallsVector,
                                    final PolymorphismKind polymorphismKind) {
      this.rewriter = rewriter;
      this.cppStd = cppStd;
      callsVectorName = nameOfAllCallsVector;
      polyKind = polymorphismKind;
   }

   @Override
   public abstract void addMockSupport(ICPPASTFunctionDefinition function);

   protected void addAllExistingBodyStmts(final IASTCompoundStatement body, final ICPPASTFunctionDefinition fun) {
      for (final IASTStatement bodyStmt : ((IASTCompoundStatement) fun.getBody()).getStatements()) {
         body.addStatement(bodyStmt.copy());
      }
   }

   protected IASTCompoundStatement createNewFunBody(final ICPPASTFunctionDefinition function) {
      final IASTCompoundStatement funBody = nodeFactory.newCompoundStatement();
      ILTISException.Unless.assignableFrom("Compound statement expected as function body", IASTCompoundStatement.class, function.getBody());
      fillFunBody(funBody, function);
      return funBody;
   }

   protected abstract void fillFunBody(IASTCompoundStatement funBody, ICPPASTFunctionDefinition fun);

   protected void addCallRegistration(final IASTCompoundStatement newBody, final ICPPASTFunctionDefinition fun) {
      final ICPPASTFunctionDeclarator funDecl = (ICPPASTFunctionDeclarator) fun.getDeclarator();
      final boolean isStatic = ASTUtil.isStatic(fun.getDeclSpecifier());
      new MemberFunCallRegistrationAdder(funDecl, isStatic, cppStd, callsVectorName).addRegistrationTo(newBody);
   }

   protected boolean isSubTypePoly() {
      return polyKind == PolymorphismKind.SubTypePoly;
   }
}
