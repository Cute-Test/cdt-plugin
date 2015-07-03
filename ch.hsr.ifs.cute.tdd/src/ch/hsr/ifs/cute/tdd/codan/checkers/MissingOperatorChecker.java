/*******************************************************************************
 * Copyright (c) 2011-2015, IFS Institute for Software, HSR Rapperswil,
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
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;
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
			if (expression instanceof ICPPASTUnaryExpression) {
				CPPASTUnaryExpression uexpr = ((CPPASTUnaryExpression) expression);
				String typename = ASTTypeUtil.getType(uexpr.getOperand().getExpressionType(), true);
				return handleUnaryOperator(expression, typename, uexpr);
			} else if (expression instanceof ICPPASTBinaryExpression) {
				ICPPASTBinaryExpression binex = (ICPPASTBinaryExpression) expression;
				String typename = ASTTypeUtil.getType(binex.getOperand1().getExpressionType(), true);
				return handleBinaryOperator(typename, binex);
			}
			return PROCESS_CONTINUE;
		}

		private int handleBinaryOperator(String typename, ICPPASTBinaryExpression binex) {
			ICPPFunction overload = binex.getOverload();
			if (overload == null && hasTypeOperand(binex) && hasKnownTypes(binex)) {
				OverloadableOperator operator = OverloadableOperator.fromBinaryExpression(binex.getOperator());
				if (operator != null) {
					String strategy = getStrategy(binex);
					reportMissingOperator(typename, binex, operator, strategy, binex.getOperand1().getExpressionType());
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
			if (	uexpr.getOverload() == null 
					&& operandDefined(uexpr) 
					&& hasNonPrimitiveType(uexpr.getOperand()) 
					&& hasKnownType(uexpr.getOperand())
					&& !isAddressOfOperator(uexpr)) {
				OverloadableOperator operator = OverloadableOperator.fromUnaryExpression(uexpr.getOperator());
				if (operator != null) {
					String strategy = getStrategy(uexpr);
					reportMissingOperator(typename, expression, operator, strategy, uexpr.getOperand().getExpressionType());
					return PROCESS_SKIP;
				}
			}
			return PROCESS_CONTINUE;
		}

		private String getStrategy(ICPPASTUnaryExpression uexpr) {
			OverloadableOperator operator = OverloadableOperator.fromUnaryExpression(uexpr.getOperator());
			return mustBeAMemberFunction(operator) ? ":memberoperator" : ":anyoperator";
		}

		private void reportMissingOperator(String typename, IASTExpression expr, OverloadableOperator operator, String strategy, IType type) {
			String operatorname = new String(operator.toCharArray()).replaceAll("operator ", "");
			CodanArguments ca = new CodanArguments(operatorname, typename, strategy);
			setNodeLocation(type, ca);
			reportProblem(ERR_ID_OperatorResolutionProblem, expr, ca.toArray());
		}

		private void setNodeLocation(IType type, CodanArguments ca) {
			if (type instanceof CPPClassType) {
				IASTFileLocation fileLocation = ((CPPClassType) type).getCompositeTypeSpecifier().getFileLocation();
				int offset = fileLocation.getNodeOffset();
				int length = fileLocation.getNodeLength();

				ca.setNodeOffset(offset);
				ca.setNodeLength(length);
			}
		}

		private boolean mustBeAMemberFunction(OverloadableOperator operator) {
			if (operator == null) {
				return false;
			}
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
			type = SemanticUtil.getUltimateTypeUptoPointers(type);
			return     type instanceof IBasicType
			        || type instanceof IEnumeration
			        || type instanceof IFunctionType
			        || type instanceof IPointerType
			        || type instanceof IArrayType;
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

		private boolean isAddressOfOperator(ICPPASTUnaryExpression uexpr) {
			return uexpr.getOperator() == IASTUnaryExpression.op_amper;
		}
	}
}
