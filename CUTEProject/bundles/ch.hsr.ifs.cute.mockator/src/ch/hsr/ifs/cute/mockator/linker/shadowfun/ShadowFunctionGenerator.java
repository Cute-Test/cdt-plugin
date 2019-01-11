package ch.hsr.ifs.cute.mockator.linker.shadowfun;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;

import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;

import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.refsupport.functions.params.ParameterNameFunDecorator;
import ch.hsr.ifs.cute.mockator.refsupport.functions.returntypes.ReturnStatementCreator;
import ch.hsr.ifs.cute.mockator.refsupport.utils.QualifiedNameCreator;


public class ShadowFunctionGenerator {

    private static final ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
    private final CppStandard            cppStd;

    public ShadowFunctionGenerator(final CppStandard cppStd) {
        this.cppStd = cppStd;
    }

    public ICPPASTFunctionDefinition createShadowedFunction(final ICPPASTFunctionDeclarator funDecl, final IASTCompoundStatement newBody) {
        final ICPPASTDeclSpecifier newDeclSpec = ASTUtil.getDeclSpec(funDecl).copy();
        ASTUtil.removeExternalStorageIfSet(newDeclSpec);
        final ICPPASTFunctionDeclarator newFunDecl = funDecl.copy();
        adjustParamNamesIfNecessary(newFunDecl);
        newFunDecl.setName(createFullyQualifiedName(funDecl));
        final ReturnStatementCreator creator = new ReturnStatementCreator(cppStd);
        newBody.addStatement(creator.createReturnStatement(funDecl, newDeclSpec));
        return nodeFactory.newFunctionDefinition(newDeclSpec, newFunDecl, newBody);
    }

    private static IASTName createFullyQualifiedName(final ICPPASTFunctionDeclarator funDecl) {
        final QualifiedNameCreator resolver = new QualifiedNameCreator(funDecl.getName());
        final ICPPASTQualifiedName qualifiedName = resolver.createQualifiedName();
        qualifiedName.addName(funDecl.getName().copy());
        return qualifiedName;
    }

    private static void adjustParamNamesIfNecessary(final ICPPASTFunctionDeclarator newFunDecl) {
        final ParameterNameFunDecorator funDecorator = new ParameterNameFunDecorator(newFunDecl);
        funDecorator.adjustParamNamesIfNecessary();
    }
}
