/*******************************************************************************
 * Copyright (c) 2011-2014, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.codan.checkers;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

import ch.hsr.ifs.cute.tdd.CodanArguments;

public class MissingOperatorChecker extends AbstractTDDChecker {

	public static final String ERR_ID_OperatorResolutionProblem = "ch.hsr.ifs.cute.tdd.codan.checkers.MissingOperatorResolutionProblem";

	@Override
	protected void runChecker(IASTTranslationUnit ast) {
		ast.accept(new MissingOperatorVisitor());
	}

	class MissingOperatorVisitor extends ASTVisitor {
		{
			shouldVisitExpressions = true;
		}

		@Override
		public int visit(IASTExpression expression) {
			String typename = ASTTypeUtil.getType(expression.getExpressionType(), true);
			if (expression instanceof ICPPASTUnaryExpression) {
				CPPASTUnaryExpression uexpr = ((CPPASTUnaryExpression) expression);
				return handleUnaryOperator(expression, typename, uexpr);
			} else if (expression instanceof ICPPASTBinaryExpression) {
				ICPPASTBinaryExpression binex = (ICPPASTBinaryExpression) expression;
				return handleBinaryOperator(typename, binex);
			}
			return PROCESS_CONTINUE;
		}

		private int handleBinaryOperator(String typename, ICPPASTBinaryExpression binex) {
			IASTImplicitName[] inames = binex.getImplicitNames();
			if (!operatorFound(inames)) {
				if (hasTypeOperand(binex) && hasKnownTypes(binex)) {
					OverloadableOperator operator = OverloadableOperator.fromBinaryExpression(binex.getOperator());
					if (operator == null || implicitlyAvailableOperation(operator, binex)) {
						return PROCESS_CONTINUE;
					}
					String strategy = getStrategy(binex);
					reportMissingOperator(typename, binex, operator, strategy);
					return PROCESS_SKIP;
				}
			}
			return PROCESS_CONTINUE;
		}

		private String getStrategy(ICPPASTBinaryExpression binex) {
			OverloadableOperator operator = OverloadableOperator.fromBinaryExpression(binex.getOperator());
			boolean freeOperator = !mustBeAMemberFunction(operator);
			boolean memberOperator = isAppropriateType(binex.getOperand1());
			if (freeOperator) {
				return memberOperator ? ":anyoperator" : ":freeoperator";
			} else {
				return memberOperator ? ":memberoperator" : ":null";
			}
		}

		private int handleUnaryOperator(IASTExpression expression, String typename, ICPPASTUnaryExpression uexpr) {
			IASTImplicitName[] inames = uexpr.getImplicitNames();
			if (!operatorFound(inames)) {
				OverloadableOperator operator = OverloadableOperator.fromUnaryExpression(uexpr.getOperator());
				if (shouldSkipUnaryOperator(uexpr, operator)) {
					return PROCESS_CONTINUE;
				}
				if (isAppropriateType(uexpr.getOperand())) {
					String strategy = getStrategy(uexpr);
					reportMissingOperator(typename, expression, operator, strategy);
					return PROCESS_SKIP;
				}
			}
			return PROCESS_CONTINUE;
		}

		private String getStrategy(ICPPASTUnaryExpression uexpr) {
			OverloadableOperator operator = OverloadableOperator.fromUnaryExpression(uexpr.getOperator());
			return mustBeAMemberFunction(operator) ? ":memberoperator" : ":anyoperator";
		}

		private boolean shouldSkipUnaryOperator(ICPPASTUnaryExpression uexpr, OverloadableOperator operator) {
			return unaryOperatorToSkip(operator) || implicitlyAvailableOperation(operator, uexpr)
					|| !operandDefined(uexpr);
		}

		private void reportMissingOperator(String typename, IASTExpression expr, OverloadableOperator operator,
				String strategy) {
			String operatorname = new String(operator.toCharArray()).replaceAll("operator ", "");
			CodanArguments ca = new CodanArguments(operatorname, typename, strategy);
			reportProblem(ERR_ID_OperatorResolutionProblem, expr, ca.toArray());
		}

		private boolean implicitlyAvailableOperation(OverloadableOperator operator, ICPPASTUnaryExpression uexpr) {
			IASTExpression operand = uexpr.getOperand();
			final IType operandType = operand.getExpressionType();
			if (operandType instanceof IPointerType || operandType instanceof IArrayType) {
				switch (operator) {
				case NOT:
				case STAR:
				case INCR:
				case DECR:
					return true;
				default:
					break;
				}
			}
			return false;
		}

		private boolean implicitlyAvailableOperation(OverloadableOperator operator, ICPPASTBinaryExpression binExpr) {
			IASTExpression operand = binExpr.getOperand1();
			final IType operandType = operand.getExpressionType();
			if (operandType instanceof IPointerType || operandType instanceof IArrayType) {
				switch (operator) {
				case ASSIGN:
				case AND:
				case OR:
				case PLUS:
				case MINUS:
				case EQUAL:
				case NOTEQUAL:
				case PLUSASSIGN:
				case MINUSASSIGN:
					return true;
				default:
					break;
				}
			}
			return false;
		}

		private boolean mustBeAMemberFunction(OverloadableOperator operator) {
			switch (operator) {
			case ASSIGN:
			case PAREN:
			case BRACKET:
			case ARROW:
				return true;
			default:
				return false;
			}
		}

		private boolean operandDefined(ICPPASTUnaryExpression uexpr) {
			IASTExpression operand = uexpr.getOperand();
			if (operand instanceof IASTIdExpression) {
				return !(((IASTIdExpression) operand).getName().resolveBinding() instanceof IProblemBinding);
			}
			return false;
		}

		private boolean unaryOperatorToSkip(OverloadableOperator operator) {
			return operator == null || operator == OverloadableOperator.AMPER;
		}

		private boolean operatorFound(IASTImplicitName[] inames) {
			if (inames.length == 1) {
				IASTImplicitName iname = inames[0];
				IBinding b = iname.resolveBinding();
				if (b instanceof ICPPFunction) {
					return true;
				}
			}
			return false;
		}

		private boolean isAppropriateType(IASTExpression operand) {
			if (operand instanceof ICPPASTLiteralExpression) {
				return false;
			}

			if (hasPrimitiveType(operand) || !hasKnownType(operand)) {
				return false;
			}

			if (operand instanceof IASTIdExpression) {
				return !(((IASTIdExpression) operand).getName().getBinding() instanceof IProblemBinding);
			}
			return false;
		}

		private boolean hasNonPrimitiveType(IASTExpression operand) {
			return !hasPrimitiveType(operand);
		}

		private boolean hasPrimitiveType(IASTExpression operand) {
			IType type = operand.getExpressionType();
			type = SemanticUtil.getUltimateType(type, true); //, SemanticUtil.TDEF | SemanticUtil.ALLCVQ);
			return type instanceof IBasicType || type instanceof IEnumeration;
		}

		private boolean hasTypeOperand(IASTBinaryExpression expression) {
			return hasNonPrimitiveType(expression.getOperand1()) || hasNonPrimitiveType(expression.getOperand2());
		}

		private boolean hasKnownTypes(IASTBinaryExpression expression) {
			return hasKnownType(expression.getOperand1()) && hasKnownType(expression.getOperand2());
		}

		private boolean hasKnownType(IASTExpression operand) {
			IType type = operand.getExpressionType();
			type = SemanticUtil.getNestedType(type, SemanticUtil.TDEF | SemanticUtil.ALLCVQ);
			return !(type instanceof ICPPUnknownType || type instanceof IProblemType);
		}
	}
}
