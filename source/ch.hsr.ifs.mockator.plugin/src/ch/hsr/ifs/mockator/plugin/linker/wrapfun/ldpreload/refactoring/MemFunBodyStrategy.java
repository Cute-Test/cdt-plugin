package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload.refactoring;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;

import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionDelegateCallCreator;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.QualifiedNameCreator;

@SuppressWarnings("restriction")
class MemFunBodyStrategy extends CommonFunBodyStrategy {

  // typedef int (Foo::*memFunType)() const;
  @Override
  protected IASTDeclarationStatement createFunTypedef(ICPPASTFunctionDeclarator funDecl) {
    ICPPASTDeclSpecifier newSimpleDeclSpec = createNewFunDeclSpec(funDecl);
    newSimpleDeclSpec.setStorageClass(IASTDeclSpecifier.sc_typedef);
    IASTSimpleDeclaration newDecl = nodeFactory.newSimpleDeclaration(newSimpleDeclSpec);
    ICPPASTQualifiedName fullyQualifiedName = getFullyQualifiedNameFor(getClassOf(funDecl));
    String typeDefName =
        String.format("(%s::*%s)", String.valueOf(fullyQualifiedName.toCharArray()), FUN_PTR);
    IASTName newName = nodeFactory.newName(typeDefName.toCharArray());
    ICPPASTFunctionDeclarator newFunDecl = nodeFactory.newFunctionDeclarator(newName);
    addParams(funDecl, newFunDecl);
    newFunDecl.setConst(funDecl.isConst());
    newDecl.addDeclarator(newFunDecl);
    return nodeFactory.newDeclarationStatement(newDecl);
  }

  private static ICPPASTCompositeTypeSpecifier getClassOf(ICPPASTFunctionDeclarator funDecl) {
    return AstUtil.getAncestorOfType(funDecl, ICPPASTCompositeTypeSpecifier.class);
  }

  private static ICPPASTQualifiedName getFullyQualifiedNameFor(IASTCompositeTypeSpecifier klass) {
    return new QualifiedNameCreator(klass.getName()).createQualifiedName();
  }

  // return (this->*origMemFun)();
  @Override
  protected IASTStatement createReturn(ICPPASTFunctionDeclarator funDecl) {
    FunctionDelegateCallCreator creator = new FunctionDelegateCallCreator(funDecl);
    return creator.createDelegate(nodeFactory.newName(String.format("(this->*%s)", ORIG_FUN)
        .toCharArray()));
  }

  // memcpy(&origFun, &tmpPtr, sizeof(&tmpPtr));
  @Override
  protected IASTStatement createReinterpretCast() {
    IASTInitializerClause[] args = new IASTInitializerClause[3];
    args[0] = createPassByPtrFor(ORIG_FUN);
    args[1] = createPassByPtrFor(TMP_PTR);
    ICPPASTUnaryExpression tmpPtr =
        nodeFactory.newUnaryExpression(IASTUnaryExpression.op_bracketedPrimary,
            createPassByPtrFor(TMP_PTR));
    args[2] = nodeFactory.newUnaryExpression(IASTUnaryExpression.op_sizeof, tmpPtr);
    IASTIdExpression memCpy =
        nodeFactory.newIdExpression(nodeFactory.newName("memcpy".toCharArray()));
    ICPPASTFunctionCallExpression memcpyFunCall =
        nodeFactory.newFunctionCallExpression(memCpy, args);
    return nodeFactory.newExpressionStatement(memcpyFunCall);
  }

  private static ICPPASTUnaryExpression createPassByPtrFor(String paramName) {
    return nodeFactory.newUnaryExpression(IASTUnaryExpression.op_amper,
        nodeFactory.newIdExpression(nodeFactory.newName(paramName.toCharArray())));
  }
}
