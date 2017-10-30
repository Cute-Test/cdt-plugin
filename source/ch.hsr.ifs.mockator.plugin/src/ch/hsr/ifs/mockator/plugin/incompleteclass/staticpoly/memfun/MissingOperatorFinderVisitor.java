package ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.memfun;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.TypeOfDependentExpression;

import ch.hsr.ifs.mockator.plugin.incompleteclass.StaticPolyMissingMemFun;


@SuppressWarnings("restriction")
class MissingOperatorFinderVisitor extends MissingMemFunVisitor {

   private final Collection<StaticPolyMissingMemFun> missingOperators;

   {
      shouldVisitExpressions = true;
   }

   public MissingOperatorFinderVisitor(ICPPASTCompositeTypeSpecifier testDouble, ICPPASTTemplateParameter templateParam,
                                       ICPPASTTemplateDeclaration sut) {
      super(testDouble, templateParam, sut);
      missingOperators = orderPreservingSet();
   }

   @Override
   public Collection<StaticPolyMissingMemFun> getMissingMemberFunctions() {
      return missingOperators;
   }

   @Override
   public int visit(IASTExpression expr) {
      if (expr instanceof ICPPASTUnaryExpression) return handleUnaryOperator(expr, (ICPPASTUnaryExpression) expr);
      else if (expr instanceof ICPPASTBinaryExpression) return handleBinaryOperator((ICPPASTBinaryExpression) expr);
      else if (expr instanceof ICPPASTFunctionCallExpression) return handleFunCallOperator((ICPPASTFunctionCallExpression) expr);
      else if (expr instanceof ICPPASTArraySubscriptExpression) return handleIndexOperator((ICPPASTArraySubscriptExpression) expr);

      return PROCESS_CONTINUE;
   }

   private int handleIndexOperator(ICPPASTArraySubscriptExpression expr) {
      if (resolvesToTemplateType(expr.getArrayExpression()) && expr.getExpressionType() instanceof TypeOfDependentExpression) {
         reportMissingOperator(expr, OverloadableOperator.BRACKET);
         return PROCESS_SKIP;
      }
      return PROCESS_CONTINUE;
   }

   private int handleFunCallOperator(ICPPASTFunctionCallExpression funCall) {
      IASTExpression functionNameExpression = funCall.getFunctionNameExpression();
      IType expressionType = funCall.getExpressionType();

      if (resolvesToTemplateType(functionNameExpression) && expressionType instanceof TypeOfDependentExpression) {
         reportMissingOperator(funCall, OverloadableOperator.PAREN);
         return PROCESS_SKIP;
      }
      return PROCESS_CONTINUE;
   }

   private boolean resolvesToTemplateType(IASTExpression expr) {
      if (!(expr instanceof IASTIdExpression)) return false;

      IBinding resolveBinding = ((IASTIdExpression) expr).getName().resolveBinding();

      if (!(resolveBinding instanceof ICPPVariable)) return false;

      IType type = ((ICPPVariable) resolveBinding).getType();
      return resolvesToTemplateParam(type);
   }

   private int handleBinaryOperator(ICPPASTBinaryExpression binEx) {
      IASTImplicitName[] iNames = binEx.getImplicitNames();

      if (wasOperatorFound(iNames)) return PROCESS_CONTINUE;

      if (isAppropriateType(binEx.getOperand1())) {
         OverloadableOperator operator = OverloadableOperator.fromBinaryExpression(binEx.getOperator());

         if (shouldSkipBinaryOperator(operator)) return PROCESS_CONTINUE;

         reportMissingOperator(binEx, operator);
         return PROCESS_SKIP;
      }

      return PROCESS_CONTINUE;
   }

   private static boolean shouldSkipBinaryOperator(OverloadableOperator operator) {
      return operator == null || isImplicitlyAvailable(operator);
   }

   private static boolean isImplicitlyAvailable(OverloadableOperator operator) {
      return operator == OverloadableOperator.ASSIGN;
   }

   private int handleUnaryOperator(IASTExpression expression, ICPPASTUnaryExpression uExpr) {
      IASTImplicitName[] iNames = uExpr.getImplicitNames();

      if (wasOperatorFound(iNames)) return PROCESS_CONTINUE;

      OverloadableOperator operator = OverloadableOperator.fromUnaryExpression(uExpr.getOperator());

      if (shouldSkipUnaryOperator(uExpr, operator)) return PROCESS_CONTINUE;

      if (isAppropriateType(uExpr.getOperand())) {
         reportMissingOperator(expression, operator);
         return PROCESS_SKIP;
      }

      return PROCESS_CONTINUE;
   }

   private static boolean shouldSkipUnaryOperator(ICPPASTUnaryExpression uExpr, OverloadableOperator operator) {
      return isUnaryOperatorToSkip(operator, uExpr) || !isOperandDefined(uExpr);
   }

   private void reportMissingOperator(IASTExpression expr, OverloadableOperator operator) {
      missingOperators.add(new Operator(expr, operator, templateParamType, getTestDoubleName()));
   }

   private static boolean isOperandDefined(ICPPASTUnaryExpression uExpr) {
      IASTExpression operand = uExpr.getOperand();

      if (operand instanceof IASTIdExpression) {
         IASTIdExpression idexpr = (IASTIdExpression) operand;

         if (idexpr.getName().resolveBinding() instanceof IProblemBinding) return false;
      }

      return true;
   }

   private static boolean isUnaryOperatorToSkip(OverloadableOperator op, ICPPASTUnaryExpression uExpr) {
      if (op == null) return true;

      IType operandType = uExpr.getOperand().getExpressionType();

      if (operandType instanceof IPointerType || operandType instanceof IArrayType) {
         switch (op) {
         case STAR:
            return true;
         default:
            return false;
         }
      }

      return false;
   }

   private static boolean wasOperatorFound(IASTImplicitName[] iNames) {
      if (iNames.length != 1) return false;

      IBinding binding = iNames[0].resolveBinding();
      return binding instanceof ICPPFunction && !(binding instanceof ICPPUnknownBinding);
   }

   private boolean isAppropriateType(IASTExpression operand) {
      if (operand instanceof ICPPASTLiteralExpression) return false;

      IType type = operand.getExpressionType();

      if (type instanceof IQualifierType) {
         type = ((IQualifierType) type).getType();
      }

      if (type instanceof IBasicType || type instanceof IEnumeration) return false;

      if (operand instanceof IASTIdExpression && isProblemBinding(operand)) return false;

      if (type instanceof TypeOfDependentExpression) {
         type = resolveTypeOfDependentExpression((TypeOfDependentExpression) type);
      }

      return resolvesToTemplateParam(type);
   }

   private boolean isProblemBinding(IASTExpression operand) {
      return ((IASTIdExpression) operand).getName().getBinding() instanceof IProblemBinding;
   }
}
