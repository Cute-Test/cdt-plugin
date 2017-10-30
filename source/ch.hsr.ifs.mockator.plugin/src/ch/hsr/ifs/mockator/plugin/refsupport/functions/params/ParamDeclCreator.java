package ch.hsr.ifs.mockator.plugin.refsupport.functions.params;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.STD_STRING;

import java.util.Map;

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
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArrayModifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.types.DeclSpecGenerator;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;


// Inspired by TDD
@SuppressWarnings("restriction")
public class ParamDeclCreator {

   private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();

   public static ICPPASTParameterDeclaration createReferenceParamFrom(String typeName, Map<String, Boolean> nameHistory) {
      IASTName name = nodeFactory.newName(typeName.toCharArray());
      ICPPASTNamedTypeSpecifier spec = nodeFactory.newTypedefNameSpecifier(name);
      spec.setConst(true);
      IASTName parameterName = new ParameterNameCreator(nameHistory).getParamName(typeName);
      IASTDeclarator decl = nodeFactory.newDeclarator(parameterName);
      decl.addPointerOperator(nodeFactory.newReferenceOperator(false));
      return nodeFactory.newParameterDeclaration(spec, decl);
   }

   public static ICPPASTParameterDeclaration createParameterFrom(IASTExpression idExpr, Map<String, Boolean> nameHistory) {
      if (idExpr instanceof ICPPASTLiteralExpression) return createParameter((ICPPASTLiteralExpression) idExpr, nameHistory);
      else if (idExpr instanceof IASTIdExpression) return createParameter((IASTIdExpression) idExpr, nameHistory);
      else if (idExpr instanceof IASTFunctionCallExpression) return createParameter((IASTFunctionCallExpression) idExpr, nameHistory);
      else return createParameter(idExpr.getExpressionType(), idExpr, nameHistory);
   }

   public static ICPPASTParameterDeclaration createParameter(IType type, IASTInitializerClause clause, Map<String, Boolean> nameHistory) {
      ICPPASTDeclSpecifier spec = createDeclSpecifier(type);
      IASTName parameterName = getParmeterName(clause, nameHistory);
      IASTDeclarator decl = getParameterDeclarator(parameterName, type, false);
      spec.setConst(true);
      return nodeFactory.newParameterDeclaration(spec, decl);
   }

   public static ICPPASTParameterDeclaration createParameter(IASTIdExpression idExpr, Map<String, Boolean> nameHistory) {
      IType type = AstUtil.getType(idExpr);
      ICPPASTDeclSpecifier spec = createDeclSpecifier(type);
      IASTName parameterName = getParmeterName(idExpr, nameHistory);
      IASTDeclarator declarator = getParameterDeclarator(parameterName, type, false);
      return nodeFactory.newParameterDeclaration(spec, declarator);
   }

   public static ICPPASTParameterDeclaration createParameter(IASTLiteralExpression litexpr, Map<String, Boolean> nameHistory) {
      boolean skipConstCharArray = false;
      ICPPASTDeclSpecifier spec = null;
      IASTName paramName = null;
      ParameterNameCreator paramNameCreator = new ParameterNameCreator(nameHistory);
      StdString stdString = new StdString();

      if (stdString.isStdString(litexpr)) {
         spec = stdString.createStdStringDecl();
         paramName = paramNameCreator.getParamName(STD_STRING);
         skipConstCharArray = true;
      } else {
         IType type = litexpr.getExpressionType();
         type = AstUtil.windDownToRealType(type, true);
         paramName = paramNameCreator.getParamName(type);
         spec = createDeclSpecifier(type);
      }

      spec.setConst(true);
      IASTDeclarator declarator = getParameterDeclarator(paramName, litexpr.getExpressionType(), skipConstCharArray);
      makeLastPtrOpConst(declarator);
      return nodeFactory.newParameterDeclaration(spec, declarator);
   }

   public static ICPPASTParameterDeclaration createParameter(IASTFunctionCallExpression call, Map<String, Boolean> nameHistory) {
      IASTExpression functionName = call.getFunctionNameExpression();

      if (functionName instanceof IASTIdExpression) {
         ICPPASTParameterDeclaration paramDecl = createParameter((IASTIdExpression) functionName, nameHistory);
         paramDecl.getDeclSpecifier().setConst(true);
         return paramDecl;
      }

      return createParameter(call.getExpressionType(), call, nameHistory);
   }

   private static IASTName getParmeterName(IASTInitializerClause clause, Map<String, Boolean> nameHistory) {
      ParameterNameCreator creator = new ParameterNameCreator(nameHistory);
      return creator.getParamName(clause);
   }

   private static ICPPASTDeclSpecifier createDeclSpecifier(IType type) {
      DeclSpecGenerator generator = new DeclSpecGenerator(type);
      ICPPASTDeclSpecifier spec = generator.getDeclSpec();
      spec.setConst(true);
      spec.setVolatile(AstUtil.hasVolatilePart(type));
      return spec;
   }

   private static boolean makeLastPtrOpConst(IASTDeclarator declarator) {
      IASTPointerOperator[] ptrOperators = declarator.getPointerOperators();

      if (ptrOperators == null) return false;

      for (int i = ptrOperators.length - 1; i >= 0; i--) {
         IASTPointerOperator currentPtrOp = ptrOperators[i];

         if (currentPtrOp instanceof IASTPointer) {
            ((IASTPointer) currentPtrOp).setConst(true);
            return true;
         }
      }

      return false;
   }

   private static IASTDeclarator getParameterDeclarator(IASTName parameterName, IType type, boolean skipConstCharArray) {
      IASTDeclarator paramDecl = assembleDeclarator(parameterName, type, skipConstCharArray);

      if (!(paramDecl instanceof ICPPASTArrayDeclarator)) {
         paramDecl.addPointerOperator(nodeFactory.newReferenceOperator(false));
      }

      return paramDecl;
   }

   private static IASTDeclarator assembleDeclarator(IASTName parameterName, IType type, boolean skipConstCharArray) {
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

   private static boolean isConstCharArray(IType type) {
      return new ConstArrayVerifier(type).isConstCharArray();
   }

   private static IASTDeclarator getPointerParameterDeclarator(IASTName parameterName, IPointerType type) {
      IType pointedType = type.getType();
      IASTDeclarator paramDecl = assembleDeclarator(parameterName, pointedType, false);
      IASTPointer ptrOperator = nodeFactory.newPointer();
      ptrOperator.setConst(type.isConst());
      ptrOperator.setVolatile(type.isVolatile());
      paramDecl.addPointerOperator(ptrOperator);
      return paramDecl;
   }

   private static ICPPASTDeclarator getArrayParameterDeclarator(IASTName parameterName) {
      ICPPASTArrayDeclarator arrayDecl = nodeFactory.newArrayDeclarator(parameterName);
      CPPASTArrayModifier arrayModifier = new CPPASTArrayModifier();
      arrayDecl.addArrayModifier(arrayModifier);
      return arrayDecl;
   }
}
