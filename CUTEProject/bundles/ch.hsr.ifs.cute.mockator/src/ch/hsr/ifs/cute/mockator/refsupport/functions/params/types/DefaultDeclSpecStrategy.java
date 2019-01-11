package ch.hsr.ifs.cute.mockator.refsupport.functions.params.types;

import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;


class DefaultDeclSpecStrategy implements DeclSpecGeneratorStrategy {

    @Override
    public ICPPASTDeclSpecifier createDeclSpec(final IType type) {
        final ICPPASTSimpleDeclSpecifier spec = nodeFactory.newSimpleDeclSpecifier();
        spec.setType(IASTSimpleDeclSpecifier.t_int);
        spec.setConst(true);
        return spec;
    }
}
