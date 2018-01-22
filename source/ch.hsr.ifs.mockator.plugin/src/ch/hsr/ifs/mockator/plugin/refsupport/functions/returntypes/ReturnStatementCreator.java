package ch.hsr.ifs.mockator.plugin.refsupport.functions.returntypes;

import static org.eclipse.cdt.core.dom.ast.IASTLiteralExpression.lk_string_literal;
import static org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_star;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;

import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;


public class ReturnStatementCreator {

   private static final ICPPNodeFactory nodeFactory  = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   private static final String          THIS_POINTER = "this";
   private final CppStandard            cppStd;
   private final String                 memberClassName;

   public ReturnStatementCreator(final CppStandard cppStd, final String memberClassName) {
      this.cppStd = cppStd;
      this.memberClassName = memberClassName;
   }

   public ReturnStatementCreator(final CppStandard cppStd) {
      this(cppStd, "");
   }

   public IASTReturnStatement createReturnStatement(final ICPPASTFunctionDeclarator funDecl, final ICPPASTDeclSpecifier specifier) {
      if (ASTUtil.isVoid(specifier)) {
         return null; // return type is void
      } else if (hasPointerReturnType(funDecl)) {
         return createNullPtr();
      } else if (isReferenceToThis(funDecl, specifier)) {
         return createDereferencedThisPointer();
      }

      return createDefaultReturn(specifier);
   }

   private static boolean hasPointerReturnType(final ICPPASTFunctionDeclarator funDecl) {
      final IASTPointerOperator[] pointerOperators = funDecl.getPointerOperators();

      if (pointerOperators.length == 0) {
         return false;
      }

      return pointerOperators[0] instanceof IASTPointer;
   }

   private IASTReturnStatement createNullPtr() {
      return nodeFactory.newReturnStatement(cppStd.getNullPtr());
   }

   private boolean isReferenceToThis(final ICPPASTFunctionDeclarator funDecl, final ICPPASTDeclSpecifier specifier) {
      return hasReferenceReturnType(funDecl) && isReturnTypeOfClassType(specifier);
   }

   private boolean isReturnTypeOfClassType(final ICPPASTDeclSpecifier specifier) {
      if (specifier instanceof IASTNamedTypeSpecifier) {
         final IASTName name = ((IASTNamedTypeSpecifier) specifier).getName();
         return name.toString().equals(memberClassName);
      }

      return false;
   }

   private static boolean hasReferenceReturnType(final ICPPASTFunctionDeclarator funDecl) {
      final IASTPointerOperator[] pointerOperators = funDecl.getPointerOperators();

      if (pointerOperators.length != 1) {
         return false;
      }

      return pointerOperators[0] instanceof ICPPASTReferenceOperator;
   }

   private static IASTReturnStatement createDereferencedThisPointer() {
      final ICPPASTLiteralExpression thisPtr = nodeFactory.newLiteralExpression(lk_string_literal, THIS_POINTER);
      final ICPPASTUnaryExpression dereferencedThisPtr = nodeFactory.newUnaryExpression(op_star, thisPtr);
      return nodeFactory.newReturnStatement(dereferencedThisPtr);
   }

   private IASTReturnStatement createDefaultReturn(final ICPPASTDeclSpecifier specifier) {
      final ICPPASTDeclSpecifier returnDeclSpec = specifier.copy();
      returnDeclSpec.setStorageClass(IASTDeclSpecifier.sc_unspecified);
      final IASTInitializer emptyInitializer = cppStd.getEmptyInitializer();
      final ICPPASTSimpleTypeConstructorExpression returnType = nodeFactory.newSimpleTypeConstructorExpression(returnDeclSpec, emptyInitializer);
      return nodeFactory.newReturnStatement(returnType);
   }
}
