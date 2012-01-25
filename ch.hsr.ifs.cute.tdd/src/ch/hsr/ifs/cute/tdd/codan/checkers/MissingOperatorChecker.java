/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.codan.checkers;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
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
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;

import ch.hsr.ifs.cute.tdd.CodanArguments;

public class MissingOperatorChecker extends AbstractTDDChecker {

	public static final String ERR_ID_OperatorResolutionProblem_HSR = "ch.hsr.ifs.cute.tdd.codan.checkers.MissingOperatorResolutionProblem_HSR"; //$NON-NLS-1$

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
				if (isAppropriateType(binex.getOperand1())) {
					OverloadableOperator operator = OverloadableOperator.fromBinaryExpression(binex);
					if (operator == null || implicitlyAvailableOperation(operator, binex)) {
						return PROCESS_CONTINUE;
					}
					reportMissingOperator(typename, binex, operator);
					return PROCESS_SKIP;
				}
			}
			return PROCESS_CONTINUE;
		}

		private int handleUnaryOperator(IASTExpression expression, String typename, CPPASTUnaryExpression uexpr) {
			IASTImplicitName[] inames = uexpr.getImplicitNames();
			if (!operatorFound(inames)) {
				OverloadableOperator operator = OverloadableOperator.fromUnaryExpression(uexpr);
				if (shouldSkipUnaryOperator(uexpr, operator)) {
					return PROCESS_CONTINUE;
				}
				if (isAppropriateType(uexpr.getOperand())) {
					reportMissingOperator(typename, expression, operator);
					return PROCESS_SKIP;
				}
			}
			return PROCESS_CONTINUE;
		}

		private boolean shouldSkipUnaryOperator(CPPASTUnaryExpression uexpr, OverloadableOperator operator) {
			return unaryOperatorToSkip(operator) || implicitlyAvailableOperation(operator, uexpr) || !operandDefined(uexpr);
		}

		private void reportMissingOperator(String typename, IASTExpression expr, OverloadableOperator operator) {
			String operatorname = new String(operator.toCharArray()).replaceAll("operator ", ""); //$NON-NLS-1$ //$NON-NLS-2$
			final String message = Messages.MissingOperatorChecker_8 + operatorname + Messages.MissingOperatorChecker_9 + typename;
			CodanArguments ca = new CodanArguments(operatorname, message, ":operator"); //$NON-NLS-1$
			reportProblem(ERR_ID_OperatorResolutionProblem_HSR, expr, ca.toArray());
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
				}
			}
			return false;
		}

		private boolean operandDefined(CPPASTUnaryExpression uexpr) {
			IASTExpression operand = uexpr.getOperand();
			if (operand instanceof CPPASTIdExpression) {
				CPPASTIdExpression idexpr = (CPPASTIdExpression) operand;
				if (idexpr.getName().resolveBinding() instanceof IProblemBinding) {
					return false;
				}
			}
			return true;
		}

		private boolean unaryOperatorToSkip(OverloadableOperator operator) {
			if (operator == null) {
				return true;
			}
			switch (operator) {
			case AMPER:
				return true;
			}
			return false;
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
			IType type = operand.getExpressionType();
			if (type instanceof CPPQualifierType) {
				type = ((CPPQualifierType) type).getType();
			}
			if (type instanceof IBasicType || type instanceof IEnumeration) {
				return false;
			}
			if (operand instanceof IASTIdExpression) {
				return !(((IASTIdExpression) operand).getName().getBinding() instanceof IProblemBinding);
			}
			return false;
		}
	}
}
