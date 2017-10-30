package ch.hsr.ifs.mockator.plugin.mockobject.support.memfun;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.mockobject.registrations.MemberFunCallRegistrationAdder;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.testdouble.MemFunMockSupportAdder;
import ch.hsr.ifs.mockator.plugin.testdouble.PolymorphismKind;


@SuppressWarnings("restriction")
abstract class AbstractMemFunMockSupport implements MemFunMockSupportAdder {

   private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();
   protected final ASTRewrite          rewriter;
   protected final CppStandard         cppStd;
   protected final String              callsVectorName;
   private final PolymorphismKind      polyKind;

   public AbstractMemFunMockSupport(ASTRewrite rewriter, CppStandard cppStd, String nameOfAllCallsVector, PolymorphismKind polymorphismKind) {
      this.rewriter = rewriter;
      this.cppStd = cppStd;
      this.callsVectorName = nameOfAllCallsVector;
      this.polyKind = polymorphismKind;
   }

   @Override
   public abstract void addMockSupport(ICPPASTFunctionDefinition function);

   protected void addAllExistingBodyStmts(IASTCompoundStatement body, ICPPASTFunctionDefinition fun) {
      for (IASTStatement bodyStmt : ((IASTCompoundStatement) fun.getBody()).getStatements()) {
         body.addStatement(bodyStmt.copy());
      }
   }

   protected IASTCompoundStatement createNewFunBody(ICPPASTFunctionDefinition function) {
      IASTCompoundStatement funBody = nodeFactory.newCompoundStatement();
      Assert.instanceOf(function.getBody(), IASTCompoundStatement.class, "Compound statement expected as function body");
      fillFunBody(funBody, function);
      return funBody;
   }

   protected abstract void fillFunBody(IASTCompoundStatement funBody, ICPPASTFunctionDefinition fun);

   protected void addCallRegistration(IASTCompoundStatement newBody, ICPPASTFunctionDefinition fun) {
      ICPPASTFunctionDeclarator funDecl = (ICPPASTFunctionDeclarator) fun.getDeclarator();
      boolean isStatic = AstUtil.isStatic(fun.getDeclSpecifier());
      new MemberFunCallRegistrationAdder(funDecl, isStatic, cppStd, callsVectorName).addRegistrationTo(newBody);
   }

   protected boolean isSubTypePoly() {
      return polyKind == PolymorphismKind.SubTypePoly;
   }
}
