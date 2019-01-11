package ch.hsr.ifs.cute.mockator.mockobject.support.memfun;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.testdouble.PolymorphismKind;


public class CtorMockSupport extends AbstractMemFunMockSupport {

    public CtorMockSupport(final ASTRewrite rewriter, final CppStandard cppStd, final String nameOfAllCallsVector,
                           final PolymorphismKind polymorphismKind) {
        super(rewriter, cppStd, nameOfAllCallsVector, polymorphismKind);
    }

    @Override
    public void addMockSupport(final ICPPASTFunctionDefinition function) {
        final ICPPASTFunctionDefinition newFun = function.copy();
        addCtorInitializer(newFun);
        newFun.setBody(createNewFunBody(function));
        rewriter.replace(function, newFun, null);
    }

    private void addCtorInitializer(final ICPPASTFunctionDefinition ctor) {
        new MockIdInitializerAdder(callsVectorName, cppStd).accept(ctor);
    }

    @Override
    protected void fillFunBody(final IASTCompoundStatement newBody, final ICPPASTFunctionDefinition function) {
        addAllExistingBodyStmts(newBody, function);

        if (!isSubTypePoly()) {
            addCallRegistration(newBody, function);
        }
    }
}
