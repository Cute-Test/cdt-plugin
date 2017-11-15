package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload.refactoring;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;

import ch.hsr.ifs.iltis.core.exception.ILTISException;


import ch.hsr.ifs.mockator.plugin.base.util.StringUtil;
import ch.hsr.ifs.mockator.plugin.linker.ItaniumMangledNameGenerator;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;


abstract class CommonFunBodyStrategy implements LdPreloadFunBodyStrategy {

   protected static final String         ORIG_FUN    = "origFun";
   protected static final String         FUN_PTR     = "funPtr";
   protected static final String         TMP_PTR     = "tmpPtr";
   protected static final ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();

   @Override
   public IASTCompoundStatement getPreloadFunBody(final CppStandard cppStd, final ICPPASTFunctionDeclarator function) {
      final IASTCompoundStatement funBody = nodeFactory.newCompoundStatement();
      funBody.addStatement(createFunTypedef(function));
      funBody.addStatement(getOrigFunPtr(cppStd));
      funBody.addStatement(getLazyInit(function));
      funBody.addStatement(createReturn(function));
      return funBody;
   }

   protected abstract IASTStatement createReturn(ICPPASTFunctionDeclarator funDecl);

   // if (!origFun) {
   // void *tmpPtr = dlsym(RTLD_NEXT, "_Z3fooi");
   // origFun = reinterpret_cast<fptr>(tmpPtr);
   // }
   private IASTIfStatement getLazyInit(final ICPPASTFunctionDeclarator function) {
      final ICPPASTUnaryExpression notExpr = nodeFactory.newUnaryExpression(IASTUnaryExpression.op_not, nodeFactory.newIdExpression(nodeFactory
            .newName(ORIG_FUN.toCharArray())));
      final IASTCompoundStatement then = nodeFactory.newCompoundStatement();
      then.addStatement(getDlSym(function));
      then.addStatement(createReinterpretCast());
      return nodeFactory.newIfStatement(notExpr, then, null);
   }

   protected abstract IASTStatement createReinterpretCast();

   // void *tmpPtr = dlsym(RTLD_NEXT, "_Z3fooi");
   private static IASTStatement getDlSym(final ICPPASTFunctionDeclarator function) {
      final IASTInitializerClause[] args = new IASTInitializerClause[2];
      args[0] = nodeFactory.newIdExpression(nodeFactory.newName("RTLD_NEXT".toCharArray()));
      args[1] = nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_string_literal, StringUtil.quote(getMangledFunName(function)));
      final ICPPASTFunctionCallExpression dlsymcall = nodeFactory.newFunctionCallExpression(nodeFactory.newIdExpression(nodeFactory.newName("dlsym"
            .toCharArray())), args);
      final ICPPASTSimpleDeclSpecifier voidDeclSpec = nodeFactory.newSimpleDeclSpecifier();
      voidDeclSpec.setType(IASTSimpleDeclSpecifier.t_void);
      final IASTSimpleDeclaration simpleDecl = nodeFactory.newSimpleDeclaration(voidDeclSpec);
      final ICPPASTDeclarator tmpPtrDecl = nodeFactory.newDeclarator(nodeFactory.newName(TMP_PTR.toCharArray()));
      tmpPtrDecl.addPointerOperator(nodeFactory.newPointer());
      tmpPtrDecl.setInitializer(nodeFactory.newEqualsInitializer(dlsymcall));
      simpleDecl.addDeclarator(tmpPtrDecl);
      return nodeFactory.newDeclarationStatement(simpleDecl);
   }

   private static String getMangledFunName(final ICPPASTFunctionDeclarator function) {
      final IBinding binding = function.getName().resolveBinding();
      ILTISException.Unless.instanceOf(binding, ICPPFunction.class, "Function expected");
      final ItaniumMangledNameGenerator mangledGenerator = new ItaniumMangledNameGenerator((ICPPFunction) binding);
      return mangledGenerator.createMangledName();
   }

   protected abstract IASTStatement createFunTypedef(ICPPASTFunctionDeclarator funDecl);

   // static funPtr origFun = nullptr;
   private static IASTDeclarationStatement getOrigFunPtr(final CppStandard cppStd) {
      final IASTName funPtr = nodeFactory.newName(FUN_PTR.toCharArray());
      final ICPPASTNamedTypeSpecifier namedTypeSpec = nodeFactory.newTypedefNameSpecifier(funPtr);
      namedTypeSpec.setStorageClass(IASTDeclSpecifier.sc_static);
      final IASTSimpleDeclaration newSimpleDecl = nodeFactory.newSimpleDeclaration(namedTypeSpec);
      final ICPPASTDeclarator newDecl = nodeFactory.newDeclarator(nodeFactory.newName(ORIG_FUN.toCharArray()));
      newDecl.setInitializer(nodeFactory.newEqualsInitializer(cppStd.getNullPtr()));
      newSimpleDecl.addDeclarator(newDecl);
      return nodeFactory.newDeclarationStatement(newSimpleDecl);
   }

   protected ICPPASTDeclSpecifier createNewFunDeclSpec(final ICPPASTFunctionDeclarator funDecl) {
      return ASTUtil.getDeclSpec(funDecl).copy();
   }

   protected void addParams(final ICPPASTFunctionDeclarator funDecl, final ICPPASTFunctionDeclarator newFunDecl) {
      for (final ICPPASTParameterDeclaration param : funDecl.getParameters()) {
         final ICPPASTParameterDeclaration newParam = param.copy();
         newParam.getDeclarator().setName(nodeFactory.newName());
         newFunDecl.addParameterDeclaration(newParam);
      }
   }
}
