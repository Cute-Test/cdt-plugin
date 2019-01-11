package ch.hsr.ifs.cute.mockator.linker.wrapfun.ldpreload.refactoring;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;

import ch.hsr.ifs.cute.mockator.refsupport.functions.FunctionDelegateCallCreator;


class FreeFunBodyStrategy extends CommonFunBodyStrategy {

    // typedef int (*fptr)();
    @Override
    protected IASTDeclarationStatement createFunTypedef(final ICPPASTFunctionDeclarator funDecl) {
        final ICPPASTDeclSpecifier newSimpleDeclSpec = createNewFunDeclSpec(funDecl);
        newSimpleDeclSpec.setStorageClass(IASTDeclSpecifier.sc_typedef);
        final IASTSimpleDeclaration newDecl = nodeFactory.newSimpleDeclaration(newSimpleDeclSpec);
        final IASTName newName = nodeFactory.newName(String.format("(*%s)", FUN_PTR).toCharArray());
        final ICPPASTFunctionDeclarator newFunDecl = nodeFactory.newFunctionDeclarator(newName);
        addParams(funDecl, newFunDecl);
        newDecl.addDeclarator(newFunDecl);
        return nodeFactory.newDeclarationStatement(newDecl);
    }

    // origFun = reinterpret_cast<funPtr>(tmpPtr);
    @Override
    protected IASTExpressionStatement createReinterpretCast() {
        final ICPPASTTypeId funPtr = nodeFactory.newTypeId(nodeFactory.newSimpleDeclSpecifier(), nodeFactory.newDeclarator(nodeFactory.newName(FUN_PTR
                .toCharArray())));
        final IASTIdExpression tmpPtr = nodeFactory.newIdExpression(nodeFactory.newName(TMP_PTR.toCharArray()));
        final ICPPASTCastExpression reinterpretCast = nodeFactory.newCastExpression(ICPPASTCastExpression.op_reinterpret_cast, funPtr, tmpPtr);
        return nodeFactory.newExpressionStatement(nodeFactory.newBinaryExpression(IASTBinaryExpression.op_assign, nodeFactory.newIdExpression(
                nodeFactory.newName(ORIG_FUN.toCharArray())), reinterpretCast));
    }

    @Override
    protected IASTStatement createReturn(final ICPPASTFunctionDeclarator funDecl) {
        final FunctionDelegateCallCreator creator = new FunctionDelegateCallCreator(funDecl);
        return creator.createDelegate(nodeFactory.newName(ORIG_FUN.toCharArray()));
    }
}
