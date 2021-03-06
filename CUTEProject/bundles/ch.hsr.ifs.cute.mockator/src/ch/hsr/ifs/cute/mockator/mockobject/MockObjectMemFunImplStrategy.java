package ch.hsr.ifs.cute.mockator.mockobject;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.cute.mockator.incompleteclass.TestDoubleMemFunImplStrategy;
import ch.hsr.ifs.cute.mockator.mockobject.registrations.MemberFunCallRegistrationAdder;
import ch.hsr.ifs.cute.mockator.mockobject.support.memfun.MockIdInitializerAdder;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;


public class MockObjectMemFunImplStrategy implements TestDoubleMemFunImplStrategy {

    private final CppStandard cppStd;
    private final MockObject  mockObject;

    public MockObjectMemFunImplStrategy(final CppStandard cppStd, final MockObject mockObject) {
        this.cppStd = cppStd;
        this.mockObject = mockObject;
    }

    @Override
    public void addCallVectorRegistration(final IASTCompoundStatement body, final ICPPASTFunctionDeclarator decl, final boolean isStatic) {
        final String name = mockObject.getNameOfAllCallsVector();
        new MemberFunCallRegistrationAdder(decl, isStatic, cppStd, name).addRegistrationTo(body);
    }

    @Override
    public void addCtorInitializer(final ICPPASTFunctionDefinition function) {
        new MockIdInitializerAdder(mockObject.getNameOfAllCallsVector(), cppStd).accept(function);
    }
}
