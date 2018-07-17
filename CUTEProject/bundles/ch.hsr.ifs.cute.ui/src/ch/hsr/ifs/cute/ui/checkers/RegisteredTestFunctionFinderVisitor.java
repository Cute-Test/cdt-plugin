/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.checkers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;


/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 *
 */
public class RegisteredTestFunctionFinderVisitor extends ASTVisitor {

   {
      shouldVisitDeclarations = true;
   }

   private final List<IBinding> registeredTests;
   private final IIndex         index;

   public RegisteredTestFunctionFinderVisitor(IIndex iIndex) {
      registeredTests = new ArrayList<>();
      this.index = iIndex;
   }

   public List<IBinding> getRegisteredFunctionNames() {
      return registeredTests;
   }

   @Override
   public int visit(IASTDeclaration declaration) {
      if (declaration instanceof IASTSimpleDeclaration) {
         IASTSimpleDeclaration simpDecl = (IASTSimpleDeclaration) declaration;
         if (simpDecl.getDeclSpecifier() instanceof ICPPASTNamedTypeSpecifier) {
            ICPPASTNamedTypeSpecifier nameDeclSpec = (ICPPASTNamedTypeSpecifier) simpDecl.getDeclSpecifier();
            if (isCuteSuite(nameDeclSpec)) {
               IASTName suiteName = simpDecl.getDeclarators()[0].getName();
               IBinding suiteBinding = suiteName.resolveBinding();
               IASTName[] suiteRefs = suiteName.getTranslationUnit().getReferences(suiteBinding);
               for (IASTName ref : suiteRefs) {
                  if (isPushBack(ref) || isPlusAssignOperator(ref)) {
                     registeredTests.add(index.adaptBinding(getRegisteredFunctionBinding(ref)));
                  }
               }
            }
         }
      }
      return super.visit(declaration);
   }

   private IBinding getRegisteredFunctionBinding(IASTName ref) {
      final IASTInitializerClause[] arguments = getAddedArgument(ref);
      if (isFunctionPushBack(arguments)) { return getFunction(arguments); }
      if (isSimpleMemberFunctionPushBack(arguments)) { return getFunctionAtArgument(arguments, 0); }
      if (isMemberFunctionPushBack(arguments)) { return getFunctionAtArgument(arguments, 1); }
      if (isMemberFunctionWithContextPushBack(arguments)) { return getFunctionAtArgument(arguments, 1); }
      if (isFunctorPushBack(arguments)) { return getFunctor(arguments); }
      return null;
   }

   private IASTInitializerClause[] getAddedArgument(IASTName ref) {
      IASTInitializerClause[] arguments = null;
      final IASTFunctionCallExpression funcCall = ASTQueries.findAncestorWithType(ref, IASTFunctionCallExpression.class);
      if (funcCall != null) {
         arguments = funcCall.getArguments();
      } else {
         final IASTBinaryExpression binaryExpression = ASTQueries.findAncestorWithType(ref, IASTBinaryExpression.class);
         return new IASTInitializerClause[] { binaryExpression.getOperand2() };
      }
      return arguments;
   }

   private IBinding getFunctor(IASTInitializerClause[] arguments) {
      IBinding targetBinding = null;
      if (isFunctorPushBack(arguments)) {
         IASTInitializerClause argument = arguments[0];
         if (argument instanceof ICPPASTFunctionCallExpression) {
            final ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression) argument;
            final IASTIdExpression idExp = (IASTIdExpression) funcCall.getFunctionNameExpression();
            targetBinding = idExp.getName().resolveBinding();
         } else if (argument instanceof IASTImplicitNameOwner) {
            IASTImplicitNameOwner constructorExpression = (IASTImplicitNameOwner) argument;
            IASTImplicitName[] implicitNames = constructorExpression.getImplicitNames();
            if (implicitNames.length > 0) {
               targetBinding = implicitNames[0].getBinding();
            }
         }
         if (targetBinding instanceof ICPPConstructor) {
            final ICPPConstructor constructorBinding = (ICPPConstructor) targetBinding;
            return constructorBinding.getClassOwner();
         } else if (targetBinding instanceof ICPPClassType) { return targetBinding; }
      }
      return targetBinding;
   }

   private boolean isFunctorPushBack(IASTInitializerClause[] arguments) {
      if (arguments.length == 1) {
         IASTInitializerClause pushbackArgument = arguments[0];
         if (pushbackArgument instanceof ICPPASTFunctionCallExpression) {
            ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression) pushbackArgument;
            IASTImplicitName[] implicitNames = funcCall.getImplicitNames();
            if (implicitNames.length != 0) { return implicitNames[0].resolveBinding() instanceof ICPPConstructor; }
         }
         return pushbackArgument instanceof ICPPASTSimpleTypeConstructorExpression;
      }
      return false;
   }

   private IBinding getFunctionAtArgument(IASTInitializerClause[] arguments, int innerArgumentNumber) {
      if (arguments[0] instanceof ICPPASTFunctionCallExpression) {
         ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression) arguments[0];
         if (funcCall.getArguments().length > innerArgumentNumber && funcCall.getArguments()[innerArgumentNumber] instanceof IASTUnaryExpression) {
            IASTUnaryExpression unExp = (IASTUnaryExpression) funcCall.getArguments()[innerArgumentNumber];
            if (unExp.getOperand() instanceof IASTIdExpression) {
               IASTIdExpression idExp = (IASTIdExpression) unExp.getOperand();
               return idExp.getName().resolveBinding();
            }
         }
      }
      return null;
   }

   private boolean isSimpleMemberFunctionPushBack(IASTInitializerClause[] arguments) {
      if (arguments.length == 1 && arguments[0] instanceof ICPPASTFunctionCallExpression) {
         ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression) arguments[0];
         return functionNameIs(funcCall, "cute::makeSimpleMemberFunctionTest");
      }
      return false;
   }

   private boolean isMemberFunctionPushBack(IASTInitializerClause[] arguments) {
      if (arguments.length == 1 && arguments[0] instanceof ICPPASTFunctionCallExpression) {
         ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression) arguments[0];
         return functionNameIs(funcCall, "cute::makeMemberFunctionTest");
      }
      return false;
   }

   private boolean isMemberFunctionWithContextPushBack(IASTInitializerClause[] arguments) {
      if (arguments.length == 1 && arguments[0] instanceof ICPPASTFunctionCallExpression) {
         ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression) arguments[0];
         return functionNameIs(funcCall, "cute::makeMemberFunctionTestWithContext");
      }
      return false;
   }

   private IBinding getFunction(IASTInitializerClause[] arguments) {
      if (!isFunctionPushBack(arguments)) { return null; }

      ICPPASTFunctionCallExpression cuteTestCall = (ICPPASTFunctionCallExpression) arguments[0];
      IASTInitializerClause[] cuteTestArguments = cuteTestCall.getArguments();

      if (cuteTestArguments.length == 0) { return null; }

      if (cuteTestArguments[0] instanceof IASTIdExpression) {
         return ((IASTIdExpression) cuteTestArguments[0]).getName().resolveBinding();
      } else if (cuteTestArguments[0] instanceof IASTUnaryExpression) {
         IASTUnaryExpression unary = (IASTUnaryExpression) cuteTestArguments[0];
         if (unary.getOperand() instanceof IASTIdExpression && unary.getOperator() == IASTUnaryExpression.op_amper) {
            return ((IASTIdExpression) unary.getOperand()).getName().resolveBinding();
         } else if (unary.getOperand() instanceof IASTUnaryExpression) {
            IASTUnaryExpression innerUnary = (IASTUnaryExpression) unary.getOperand();
            if (innerUnary.getOperand() instanceof IASTIdExpression) { return ((IASTIdExpression) innerUnary.getOperand()).getName()
                  .resolveBinding(); }
         }
      }

      return null;
   }

   private boolean isFunctionPushBack(IASTInitializerClause[] arguments) {
      if (arguments.length == 1 && arguments[0] instanceof ICPPASTFunctionCallExpression) {
         ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression) arguments[0];
         return functionNameIs(funcCall, "cute::test");
      }
      return false;
   }

   protected boolean functionNameIs(ICPPASTFunctionCallExpression funcCall, String methodName) {
      boolean isIdExpression = funcCall.getFunctionNameExpression() instanceof IASTIdExpression;
      return isIdExpression && ((IASTIdExpression) funcCall.getFunctionNameExpression()).getName().toString().startsWith(methodName);
   }

   private boolean isPushBack(IASTName ref) {
      IASTFunctionCallExpression funcCall = ASTQueries.findAncestorWithType(ref, IASTFunctionCallExpression.class);
      if (funcCall != null) {
         if (funcCall.getFunctionNameExpression() instanceof IASTFieldReference) {
            IASTFieldReference idExp = (IASTFieldReference) funcCall.getFunctionNameExpression();
            if (idExp.getFieldName().toString().equals("push_back")) { return true; }
         }
      }
      return false;
   }

   private boolean isPlusAssignOperator(IASTName ref) {
      IASTBinaryExpression binaryExpression = ASTQueries.findAncestorWithType(ref, IASTBinaryExpression.class);
      if (binaryExpression != null) { return binaryExpression.getOperator() == IASTBinaryExpression.op_plusAssign; }
      return false;
   }

   public static boolean isCuteSuite(IASTNamedTypeSpecifier typeSpec) {
      IASTName typeName = typeSpec.getName();

      if ("cute::suite".equals(typeName.toString())) return true;

      IBinding typeBinding = typeName.resolveBinding();
      if (typeBinding instanceof ITypedef) {
         ITypedef typeDef = (ITypedef) typeBinding;
         return "suite".equals(typeDef.getName()) && "cute".equals(typeDef.getOwner().getName());
      } else if (typeBinding instanceof ICPPClassType) {
         ICPPClassType type = (ICPPClassType) typeBinding;
         return "suite".equals(type.getName()) && "cute".equals(type.getOwner().getName());

      } else return false;

   }

}
