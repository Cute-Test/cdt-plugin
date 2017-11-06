package ch.hsr.ifs.mockator.plugin.mockobject;

import java.util.function.Consumer;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.mockator.plugin.mockobject.registrations.MemberFunCallRegistrationAdder;
import ch.hsr.ifs.mockator.plugin.mockobject.support.memfun.MockIdInitializerAdder;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.testdouble.PolymorphismKind;

class DefaultCtorMockSupportAdder implements Consumer<ICPPASTFunctionDefinition> {

  private final CppStandard cppStd;
  private final PolymorphismKind polymorphismKind;
  private final String callsVectorName;

  public DefaultCtorMockSupportAdder(final CppStandard cppStd, final PolymorphismKind polyKind, final String callsVectorName) {
    this.cppStd = cppStd;
    this.polymorphismKind = polyKind;
    this.callsVectorName = callsVectorName;
  }

  @Override
  public void accept(final ICPPASTFunctionDefinition defaultCtor) {
    addCtorInitializer(defaultCtor);
    addRegistrationIfNecessary(defaultCtor);
  }

  private void addRegistrationIfNecessary(final ICPPASTFunctionDefinition defaultCtor) {
    if (polymorphismKind != PolymorphismKind.StaticPoly)
      return;

    final ICPPASTFunctionDeclarator funDecl = (ICPPASTFunctionDeclarator) defaultCtor.getDeclarator();
    final IASTCompoundStatement ctorBody = (IASTCompoundStatement) defaultCtor.getBody();
    final boolean isStatic = false;
    new MemberFunCallRegistrationAdder(funDecl, isStatic, cppStd, callsVectorName).addRegistrationTo(ctorBody);
  }

  private void addCtorInitializer(final ICPPASTFunctionDefinition defaultCtor) {
    new MockIdInitializerAdder(callsVectorName, cppStd).accept(defaultCtor);
  }
}
