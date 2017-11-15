package ch.hsr.ifs.mockator.plugin.refsupport.functions.params;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.STD_STRING;

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArrayModifier;

import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.types.DeclSpecGenerator;
import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;


// Inspired by TDD
public class ParamDeclCreator {

   private static final ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();

   public static ICPPASTParameterDeclaration createReferenceParamFrom(final String typeName, final Map<String, Boolean> nameHistory) {
      final IASTName name = nodeFactory.newName(typeName.toCharArray());
      final ICPPASTNamedTypeSpecifier spec = nodeFactory.newTypedefNameSpecifier(name);
      spec.setConst(true);
      final IASTName parameterName = new ParameterNameCreator(nameHistory).getParamName(typeName);
      final IASTDeclarator decl = nodeFactory.newDeclarator(parameterName);
      decl.addPointerOperator(nodeFactory.newReferenceOperator(false));
      return nodeFactory.newParameterDeclaration(spec, decl);
   }

   public static ICPPASTParameterDeclaration createParameterFrom(final IASTExpression idExpr, final Map<String, Boolean> nameHistory) {
      if (idExpr instanceof ICPPASTLiteralExpression) {
         return createParameter((ICPPASTLiteralExpression) idExpr, nameHistory);
      } else if (idExpr instanceof IASTIdExpression) {
         return createParameter((IASTIdExpression) idExpr, nameHistory);
      } else if (idExpr instanceof IASTFunctionCallExpression) {
         return createParameter((IASTFunctionCallExpression) idExpr, nameHistory);
      } else {
         return createParameter(idExpr.getExpressionType(), idExpr, nameHistory);
      }
   }

   public static ICPPASTParameterDeclaration createParameter(final IType type, final IASTInitializerClause clause,
         final Map<String, Boolean> nameHistory) {
      final ICPPASTDeclSpecifier spec = createDeclSpecifier(type);
      final IASTName parameterName = getParmeterName(clause, nameHistory);
      final IASTDeclarator decl = getParameterDeclarator(parameterName, type, false);
      spec.setConst(true);
      return nodeFactory.newParameterDeclaration(spec, decl);
   }

   public static ICPPASTParameterDeclaration createParameter(final IASTIdExpression idExpr, final Map<String, Boolean> nameHistory) {
      final IType type = ASTUtil.getType(idExpr);
      final ICPPASTDeclSpecifier spec = createDeclSpecifier(type);
      final IASTName parameterName = getParmeterName(idExpr, nameHistory);
      final IASTDeclarator declarator = getParameterDeclarator(parameterName, type, false);
      return nodeFactory.newParameterDeclaration(spec, declarator);
   }

   public static ICPPASTParameterDeclaration createParameter(final IASTLiteralExpression litexpr, final Map<String, Boolean> nameHistory) {
      boolean skipConstCharArray = false;
      ICPPASTDeclSpecifier spec = null;
      IASTName paramName = null;
      final ParameterNameCreator paramNameCreator = new ParameterNameCreator(nameHistory);
      final StdString stdString = new StdString();

      if (stdString.isStdString(litexpr)) {
         spec = stdString.createStdStringDecl();
         paramName = paramNameCreator.getParamName(STD_STRING);
         skipConstCharArray = true;
      } else {
         IType type = litexpr.getExpressionType();
         type = ASTUtil.windDownToRealType(type, true);
         paramName = paramNameCreator.getParamName(type);
         spec = createDeclSpecifier(type);
      }

      spec.setConst(true);
      final IASTDeclarator declarator = getParameterDeclarator(paramName, litexpr.getExpressionType(), skipConstCharArray);
      makeLastPtrOpConst(declarator);
      return nodeFactory.newParameterDeclaration(spec, declarator);
   }

   public static ICPPASTParameterDeclaration createParameter(final IASTFunctionCallExpression call, final Map<String, Boolean> nameHistory) {
      final IASTExpression functionName = call.getFunctionNameExpression();

      if (functionName instanceof IASTIdExpression) {
         final ICPPASTParameterDeclaration paramDecl = createParameter((IASTIdExpression) functionName, nameHistory);
         paramDecl.getDeclSpecifier().setConst(true);
         return paramDecl;
      }

      return createParameter(call.getExpressionType(), call, nameHistory);
   }

   private static IASTName getParmeterName(final IASTInitializerClause clause, final Map<String, Boolean> nameHistory) {
      final ParameterNameCreator creator = new ParameterNameCreator(nameHistory);
      return creator.getParamName(clause);
   }

   private static ICPPASTDeclSpecifier createDeclSpecifier(final IType type) {
      final DeclSpecGenerator generator = new DeclSpecGenerator(type);
      final ICPPASTDeclSpecifier spec = generator.getDeclSpec();
      spec.setConst(true);
      spec.setVolatile(ASTUtil.hasVolatilePart(type));
      return spec;
   }

   private static boolean makeLastPtrOpConst(final IASTDeclarator declarator) {
      final IASTPointerOperator[] ptrOperators = declarator.getPointerOperators();

      if (ptrOperators == null) {
         return false;
      }

      for (int i = ptrOperators.length - 1; i >= 0; i--) {
         final IASTPointerOperator currentPtrOp = ptrOperators[i];

         if (currentPtrOp instanceof IASTPointer) {
            ((IASTPointer) currentPtrOp).setConst(true);
            return true;
         }
      }

      return false;
   }

   private static IASTDeclarator getParameterDeclarator(final IASTName parameterName, final IType type, final boolean skipConstCharArray) {
      final IASTDeclarator paramDecl = assembleDeclarator(parameterName, type, skipConstCharArray);

      if (!(paramDecl instanceof ICPPASTArrayDeclarator)) {
         paramDecl.addPointerOperator(nodeFactory.newReferenceOperator(false));
      }

      return paramDecl;
   }

   private static IASTDeclarator assembleDeclarator(final IASTName parameterName, final IType type, final boolean skipConstCharArray) {
      IASTDeclarator paramDecl;
      if (type instanceof IPointerType) {
         paramDecl = getPointerParameterDeclarator(parameterName, (IPointerType) type);
      } else if (type instanceof IArrayType && !(skipConstCharArray && isConstCharArray(type))) {
         paramDecl = getArrayParameterDeclarator(parameterName);
      } else {
         paramDecl = nodeFactory.newDeclarator(parameterName);
      }
      return paramDecl;
   }

   private static boolean isConstCharArray(final IType type) {
      return new ConstArrayVerifier(type).isConstCharArray();
   }

   private static IASTDeclarator getPointerParameterDeclarator(final IASTName parameterName, final IPointerType type) {
      final IType pointedType = type.getType();
      final IASTDeclarator paramDecl = assembleDeclarator(parameterName, pointedType, false);
      final IASTPointer ptrOperator = nodeFactory.newPointer();
      ptrOperator.setConst(type.isConst());
      ptrOperator.setVolatile(type.isVolatile());
      paramDecl.addPointerOperator(ptrOperator);
      return paramDecl;
   }

   private static ICPPASTDeclarator getArrayParameterDeclarator(final IASTName parameterName) {
      final ICPPASTArrayDeclarator arrayDecl = nodeFactory.newArrayDeclarator(parameterName);
      final CPPASTArrayModifier arrayModifier = new CPPASTArrayModifier();
      arrayDecl.addArrayModifier(arrayModifier);
      return arrayDecl;
   }
}
