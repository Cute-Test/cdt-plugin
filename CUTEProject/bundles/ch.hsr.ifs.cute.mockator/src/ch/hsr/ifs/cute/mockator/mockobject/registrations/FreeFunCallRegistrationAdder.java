package ch.hsr.ifs.cute.mockator.mockobject.registrations;

import static ch.hsr.ifs.cute.mockator.MockatorConstants.CALL;
import static ch.hsr.ifs.cute.mockator.MockatorConstants.MOCKATOR_NS;
import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.array;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;

import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;

import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;


public class FreeFunCallRegistrationAdder extends AbstractFunCallRegistrationAdder {

    private final String callsVectorName;

    public FreeFunCallRegistrationAdder(final ICPPASTFunctionDeclarator fun, final CppStandard cppStd, final String name) {
        super(fun, cppStd);
        callsVectorName = name;
    }

    @Override
    protected String getNameForCallsVector() {
        return callsVectorName;
    }

    @Override
    protected IASTExpression getPushBackOwner() {
        return createCallSequence();
    }

    @Override
    protected String getNameForCall() {
        return ASTUtil.getQfName(array(MOCKATOR_NS, CALL));
    }
}
