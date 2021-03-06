package ch.hsr.ifs.cute.mockator.refsupport.functions.params.types;

import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;

import ch.hsr.ifs.iltis.core.exception.ILTISException;


class BasicTypeDeclSpecStrategy implements DeclSpecGeneratorStrategy {

    @Override
    public ICPPASTDeclSpecifier createDeclSpec(final IType type) {
        ILTISException.Unless.assignableFrom("This strategy can only handle basic types", IBasicType.class, type);
        final ICPPASTSimpleDeclSpecifier simpleDeclSpec = nodeFactory.newSimpleDeclSpecifier();
        final IBasicType basicType = (IBasicType) type;
        simpleDeclSpec.setType(basicType.getKind());
        simpleDeclSpec.setSigned(basicType.isSigned());
        simpleDeclSpec.setUnsigned(basicType.isUnsigned());
        return simpleDeclSpec;
    }
}
