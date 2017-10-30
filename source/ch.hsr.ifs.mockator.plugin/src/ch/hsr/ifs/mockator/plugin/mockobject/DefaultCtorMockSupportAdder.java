package ch.hsr.ifs.mockator.plugin.mockobject;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.mockobject.registrations.MemberFunCallRegistrationAdder;
import ch.hsr.ifs.mockator.plugin.mockobject.support.memfun.MockIdInitializerAdder;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.testdouble.PolymorphismKind;


class DefaultCtorMockSupportAdder implements F1V<ICPPASTFunctionDefinition> {

   private final CppStandard      cppStd;
   private final PolymorphismKind polymorphismKind;
   private final String           callsVectorName;

   public DefaultCtorMockSupportAdder(CppStandard cppStd, PolymorphismKind polyKind, String callsVectorName) {
      this.cppStd = cppStd;
      this.polymorphismKind = polyKind;
      this.callsVectorName = callsVectorName;
   }

   @Override
   public void apply(ICPPASTFunctionDefinition defaultCtor) {
      addCtorInitializer(defaultCtor);
      addRegistrationIfNecessary(defaultCtor);
   }

   private void addRegistrationIfNecessary(ICPPASTFunctionDefinition defaultCtor) {
      if (polymorphismKind != PolymorphismKind.StaticPoly) return;

      ICPPASTFunctionDeclarator funDecl = (ICPPASTFunctionDeclarator) defaultCtor.getDeclarator();
      IASTCompoundStatement ctorBody = (IASTCompoundStatement) defaultCtor.getBody();
      final boolean isStatic = false;
      new MemberFunCallRegistrationAdder(funDecl, isStatic, cppStd, callsVectorName).addRegistrationTo(ctorBody);
   }

   private void addCtorInitializer(ICPPASTFunctionDefinition defaultCtor) {
      new MockIdInitializerAdder(callsVectorName, cppStd).apply(defaultCtor);
   }
}
