package ch.hsr.ifs.mockator.plugin.refsupport.functions.returntypes;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.TypeOfDependentExpression;

import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.iltis.core.functional.OptHelper;
import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;

import ch.hsr.ifs.mockator.plugin.refsupport.utils.NodeContainer;


@SuppressWarnings("restriction")
public class ReturnTypeDeducer {

   private static final ICPPNodeFactory    nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   private final ICPPASTFunctionDeclarator funDecl;
   private final IType                     injectedType;
   private final String                    memberClassName;

   public ReturnTypeDeducer(final ICPPASTFunctionDeclarator funDecl, final IType injectedType, final String memberClassName) {
      this.funDecl = funDecl;
      this.injectedType = injectedType;
      this.memberClassName = memberClassName;
   }

   public ICPPASTDeclSpecifier determineReturnType(final IASTExpression funCall) {
      return OptHelper.returnIfPresentElse(findPossibleReturnType(funCall), (returnType) -> returnType.copy(), () -> createDefaultReturnType());
   }

   private Optional<ICPPASTDeclSpecifier> findPossibleReturnType(final IASTExpression funCall) {
      final ReturnTypeFinderVisitor visitor = new ReturnTypeFinderVisitor(injectedType, memberClassName, funDecl);
      getNodeToAnalyse(funCall).accept(visitor);
      return visitor.getReturnType();
   }

   private static IASTNode getNodeToAnalyse(final IASTExpression funCall) {
      final IASTStatement stmt = ASTUtil.getAncestorOfType(funCall, IASTStatement.class);
      ILTISException.Unless.notNull(stmt, "Could not determine return type for missing function");
      return stmt;
   }

   private static ICPPASTDeclSpecifier createDefaultReturnType() {
      final ICPPASTSimpleDeclSpecifier voidType = nodeFactory.newSimpleDeclSpecifier();
      voidType.setType(IASTSimpleDeclSpecifier.t_void);
      return voidType;
   }

   // Inspired by TDD
   private static class ReturnTypeFinderVisitor extends ASTVisitor {

      private final NodeContainer<ICPPASTDeclSpecifier> returnType;
      private final IType                               injectedType;
      private final ICPPASTFunctionDeclarator           funDeclToAdapt;
      private final String                              memberClassName;

      {
         shouldVisitStatements = true;
         shouldVisitExpressions = true;
      }

      public ReturnTypeFinderVisitor(final IType injectedType, final String memberClassName, final ICPPASTFunctionDeclarator funDeclToAdapt) {
         this.injectedType = injectedType;
         this.memberClassName = memberClassName;
         this.funDeclToAdapt = funDeclToAdapt;
         returnType = new NodeContainer<>();
      }

      public Optional<ICPPASTDeclSpecifier> getReturnType() {
         return returnType.getNode();
      }

      @Override
      public int visit(final IASTExpression expression) {
         if (expression instanceof ICPPASTUnaryExpression) {
            final IASTExpression operand = ((ICPPASTUnaryExpression) expression).getOperand();

            if (operand instanceof ICPPASTLiteralExpression) {
               return handleType(operand.getExpressionType());
            } else if (operand instanceof ICPPASTFunctionCallExpression) {
               return handleType(operand.getExpressionType());
            } else if (operand instanceof ICPPASTBinaryExpression) {
               return handleBinaryExpression((ICPPASTBinaryExpression) operand);
            } else if (operand instanceof ICPPASTUnaryExpression) {
               return handleUnaryExpression((ICPPASTUnaryExpression) operand);
            }
         } else if (expression instanceof ICPPASTBinaryExpression) {
            return handleBinaryExpression((ICPPASTBinaryExpression) expression);
         }

         return PROCESS_CONTINUE;
      }

      private int handleType(final IType type) {
         ICPPASTDeclSpecifier newReturnType;

         if (type instanceof ICPPBasicType) {
            newReturnType = createSimpleDecl(type);
         } else if (type instanceof ICPPClassType) {
            final ICPPClassType classType = (ICPPClassType) type;
            newReturnType = createNamedTypeSpec(classType.getName());
         } else if (type instanceof ITypedef) {
            final IType underlyingType = ((ITypedef) type).getType();
            newReturnType = createNamedTypeSpec(underlyingType.toString());
         } else if (type instanceof ICPPTemplateTypeParameter && refersToInjectedType((ICPPTemplateTypeParameter) type)) {
            newReturnType = createNamedTypeSpec(memberClassName);
         } else {
            return PROCESS_CONTINUE;
         }

         returnType.setNode(newReturnType.copy());
         return PROCESS_ABORT;
      }

      private boolean refersToInjectedType(final ICPPTemplateTypeParameter type) {
         return type.equals(injectedType);
      }

      @Override
      public int visit(final IASTStatement statement) {
         if (statement instanceof IASTCompoundStatement) {
            return PROCESS_CONTINUE;
         }

         if (statement instanceof IASTDeclarationStatement) {
            return handleDeclStatement((IASTDeclarationStatement) statement);
         } else if (statement instanceof IASTExpressionStatement) {
            return handleExprStatement((IASTExpressionStatement) statement);
         } else if (statement instanceof IASTIfStatement) {
            return handleIfStatement((IASTIfStatement) statement);
         } else if (statement instanceof IASTReturnStatement) {
            return handleReturnStatement((IASTReturnStatement) statement);
         }

         return PROCESS_CONTINUE;
      }

      private int handleDeclStatement(final IASTDeclarationStatement statement) {
         final IASTDeclaration declaration = statement.getDeclaration();

         if (!(declaration instanceof IASTSimpleDeclaration)) {
            return PROCESS_CONTINUE;
         }

         final IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) declaration;
         ICPPASTDeclSpecifier declSpecifier = (ICPPASTDeclSpecifier) simpleDecl.getDeclSpecifier();

         if (declSpecifier instanceof ICPPASTNamedTypeSpecifier) {
            final IBinding binding = ((ICPPASTNamedTypeSpecifier) declSpecifier).getName().resolveBinding();

            if (binding instanceof ICPPTemplateTypeParameter && refersToInjectedType((ICPPTemplateTypeParameter) binding)) {
               declSpecifier = createNamedTypeSpec(memberClassName);
            }
         }

         setPointerReturnType(getPointersInDecl(simpleDecl));
         returnType.setNode(declSpecifier);
         return PROCESS_ABORT;
      }

      private int handleExprStatement(final IASTExpressionStatement exprStatement) {
         final IASTExpression expr = exprStatement.getExpression();

         if (expr instanceof ICPPASTBinaryExpression) {
            return handleBinaryExpression((ICPPASTBinaryExpression) expr);
         } else if (expr instanceof ICPPASTUnaryExpression) {
            return handleUnaryExpression((ICPPASTUnaryExpression) expr);
         }

         return PROCESS_CONTINUE;
      }

      private int handleReturnStatement(final IASTReturnStatement returnStmt) {
         if (isReturnTypeOfFunApplicable(returnStmt)) {
            return useReturnTypeOfParentFunction(returnStmt);
         }

         return PROCESS_CONTINUE;
      }

      private static boolean isReturnTypeOfFunApplicable(final IASTReturnStatement returnStmt) {
         final IASTExpression returnVal = returnStmt.getReturnValue();
         return returnVal instanceof ICPPASTFunctionCallExpression || returnVal instanceof ICPPASTUnaryExpression;
      }

      private int useReturnTypeOfParentFunction(final IASTReturnStatement returnStmt) {
         final ICPPASTFunctionDefinition function = ASTUtil.getAncestorOfType(returnStmt, ICPPASTFunctionDefinition.class);
         final ICPPASTFunctionDeclarator funDecl = (ICPPASTFunctionDeclarator) function.getDeclarator();
         setPointerReturnType(getPointers(funDecl));
         final ICPPASTDeclSpecifier declSpecifier = (ICPPASTDeclSpecifier) function.getDeclSpecifier();

         if (declSpecifier instanceof ICPPASTNamedTypeSpecifier) {
            final ICPPASTNamedTypeSpecifier typeSpec = (ICPPASTNamedTypeSpecifier) declSpecifier;
            final IBinding binding = typeSpec.getName().resolveBinding();

            if (binding instanceof ITypedef) {
               return handleType((ITypedef) binding);
            }
         }

         returnType.setNode(declSpecifier.copy());
         return PROCESS_ABORT;
      }

      private int handleIfStatement(final IASTIfStatement statement) {
         final IASTExpression expr = statement.getConditionExpression();

         if (expr instanceof ICPPASTBinaryExpression) {
            return handleBinaryExpression((ICPPASTBinaryExpression) expr);
         } else if (expr instanceof ICPPASTUnaryExpression) {
            return handleUnaryExpression((ICPPASTUnaryExpression) expr);
         } else if (expr instanceof ICPPASTFunctionCallExpression) {
            return handleType(CPPBasicType.BOOLEAN);
         }

         return PROCESS_CONTINUE;
      }

      private int handleUnaryExpression(final ICPPASTUnaryExpression unaryExp) {
         IType unaryExpType = unaryExp.getExpressionType();
         final IASTExpression operand = unaryExp.getOperand();
         final IType opType = operand.getExpressionType();

         if (operand instanceof ICPPASTUnaryExpression) {
            handleType(((ICPPASTUnaryExpression) operand).getOperand().getExpressionType());
         } else if (operand instanceof IASTIdExpression) {
            unaryExpType = getType((IASTIdExpression) operand);

            if (unaryExpType instanceof ITypedef) {
               return handleType(unaryExpType);
            }
         } else if (opType instanceof ICPPTemplateTypeParameter) {
            return handleType(opType);
         } else if (unaryExpType instanceof ICPPUnknownType && unaryExp.getParent() instanceof IASTIfStatement) {
            return handleType(CPPBasicType.BOOLEAN);
         }

         return handleType(unaryExpType);
      }

      private int handleBinaryExpression(ICPPASTBinaryExpression binExp) {
         final IASTExpression operand1 = binExp.getOperand1();
         final IASTExpression operand2 = binExp.getOperand2();

         if (operand1 instanceof ICPPASTBinaryExpression) {
            binExp = (ICPPASTBinaryExpression) operand1;
         } else if (operand2 instanceof ICPPASTBinaryExpression) {
            binExp = (ICPPASTBinaryExpression) operand2;
         }

         final IType type = getType(binExp, operand1, operand2);
         return handleType(type);
      }

      private static IType getType(final ICPPASTBinaryExpression binExp, final IASTExpression operand1, final IASTExpression operand2) {
         IType type = binExp.getExpressionType();

         if (type instanceof TypeOfDependentExpression) {
            type = operand1.getExpressionType();

            if (type instanceof TypeOfDependentExpression) {
               type = operand2.getExpressionType();
            }
         }

         if (operand1 instanceof IASTIdExpression) {
            type = getType((IASTIdExpression) operand1);
         }

         return type;
      }

      private static IType getType(final IASTIdExpression idExpr) {
         final IASTName name = idExpr.getName();
         final IBinding var = name.resolveBinding();

         if (var instanceof IVariable) {
            return ((IVariable) var).getType();
         }

         return null;
      }

      private static ICPPASTSimpleDeclSpecifier createSimpleDecl(final IType type) {
         final ICPPASTSimpleDeclSpecifier simpleType = nodeFactory.newSimpleDeclSpecifier();
         simpleType.setType(((ICPPBasicType) type).getKind());
         return simpleType;
      }

      private static ICPPASTNamedTypeSpecifier createNamedTypeSpec(final String typeName) {
         final IASTName name = nodeFactory.newName(typeName.toCharArray());
         return nodeFactory.newTypedefNameSpecifier(name);
      }

      private static List<IASTPointerOperator> getPointers(final ICPPASTFunctionDeclarator funDecl) {
         return list(funDecl.getPointerOperators());
      }

      private static List<IASTPointerOperator> getPointersInDecl(final IASTSimpleDeclaration simpleDecl) {
         final IASTDeclarator[] declarators = simpleDecl.getDeclarators();

         if (declarators.length == 0) {
            return new ArrayList<>();
         }

         return list(declarators[0].getPointerOperators());
      }

      private void setPointerReturnType(final Collection<IASTPointerOperator> pointers) {
         clearExistingFunDeclPointers();
         addNewPointersToFunDecl(pointers);
      }

      private void clearExistingFunDeclPointers() {
         final IASTPointerOperator[] pointerOperators = funDeclToAdapt.getPointerOperators();
         for (int i = 0; i < pointerOperators.length; i++) {
            pointerOperators[i] = null;
         }
      }

      private void addNewPointersToFunDecl(final Collection<IASTPointerOperator> pointers) {
         for (final IASTPointerOperator pointer : pointers) {
            funDeclToAdapt.addPointerOperator(pointer.copy());
         }
      }
   }
}
