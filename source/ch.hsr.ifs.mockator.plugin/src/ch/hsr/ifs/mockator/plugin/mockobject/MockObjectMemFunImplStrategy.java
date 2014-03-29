package ch.hsr.ifs.mockator.plugin.mockobject;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFunImplStrategy;
import ch.hsr.ifs.mockator.plugin.mockobject.registrations.MemberFunCallRegistrationAdder;
import ch.hsr.ifs.mockator.plugin.mockobject.support.memfun.MockIdInitializerAdder;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;

public class MockObjectMemFunImplStrategy implements TestDoubleMemFunImplStrategy {
  private final CppStandard cppStd;
  private final MockObject mockObject;

  public MockObjectMemFunImplStrategy(CppStandard cppStd, MockObject mockObject) {
    this.cppStd = cppStd;
    this.mockObject = mockObject;
  }

  @Override
  public void addCallVectorRegistration(IASTCompoundStatement body, ICPPASTFunctionDeclarator decl,
      boolean isStatic) {
    String name = mockObject.getNameOfAllCallsVector();
    new MemberFunCallRegistrationAdder(decl, isStatic, cppStd, name).addRegistrationTo(body);
  }

  @Override
  public void addCtorInitializer(ICPPASTFunctionDefinition function) {
    new MockIdInitializerAdder(mockObject.getNameOfAllCallsVector(), cppStd).apply(function);
  }
}
