package ch.hsr.ifs.cute.mockator.fakeobject;

import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.mockator.incompleteclass.DefaultCtorProvider;
import ch.hsr.ifs.cute.mockator.incompleteclass.TestDoubleMemFunImplStrategy;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.testdouble.entities.AbstractTestDouble;


public class FakeObject extends AbstractTestDouble {

    public FakeObject(final ICPPASTCompositeTypeSpecifier clazz) {
        super(clazz);
    }

    @Override
    public void addAdditionalCtorSupport(final ICPPASTFunctionDefinition defaultCtor, final CppStandard cppStd) {}

    @Override
    public DefaultCtorProvider getDefaultCtorProvider(final CppStandard cppStd) {
        return new FakeObjectDefaultCtorProvider(getKlass());
    }

    @Override
    protected TestDoubleMemFunImplStrategy getImplStrategy(final CppStandard cppStd) {
        return new FakeObjectMemFunImplStrategy();
    }

    @Override
    public void addToNamespace(final ICPPASTNamespaceDefinition parentNs, final IASTSimpleDeclaration testDouble,
            final ICPPASTCompositeTypeSpecifier testDoubleToMove, final CppStandard cppStd, final ASTRewrite rewriter) {
        parentNs.addDeclaration(testDouble);
    }
}
