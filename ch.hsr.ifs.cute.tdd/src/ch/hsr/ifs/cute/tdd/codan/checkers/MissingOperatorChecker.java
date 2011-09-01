/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.codan.checkers;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTIfStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;

import ch.hsr.ifs.cute.tdd.CodanArguments;

@SuppressWarnings("restriction")
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
			if (expression instanceof ICPPASTUnaryExpression) {
				CPPASTUnaryExpression uexpr = ((CPPASTUnaryExpression) expression);
				IASTImplicitName[] inames = uexpr.getImplicitNames();
				if (!operatorFound(inames)) {
					OverloadableOperator operator = OverloadableOperator.fromUnaryExpression(uexpr);
					if (isCuteASSERTEQUALS(expression, operator) ||isCuteAssert(expression, operator)) {
						IASTIdExpression idexpr = getLastIDExpression(expression);
						if (idexpr == null) {
							return PROCESS_CONTINUE;
						}
						IASTNode parent = idexpr.getParent();
						if (parent instanceof ICPPASTUnaryExpression) {
							uexpr = (CPPASTUnaryExpression) parent;
							inames = uexpr.getImplicitNames();
							if (operatorFound(inames)) {
								return PROCESS_SKIP;
							}
							operator = OverloadableOperator.fromUnaryExpression(uexpr);
							expression = uexpr;
							if (operator == null) {
								return PROCESS_CONTINUE;
							}
						}
					}
					if (operator == null) {
						return PROCESS_CONTINUE;
					}
					if (operatorToSkip(operator)) {
						return PROCESS_CONTINUE;
					}
					if (!operandDefined(uexpr)) {
						return PROCESS_CONTINUE;
					}
					if (implicitlyAvailableOperation(operator, uexpr)) {
						return PROCESS_CONTINUE;
					}
					String operatorname = new String(operator.toCharArray()).replaceAll("operator ", ""); //$NON-NLS-1$ //$NON-NLS-2$
					if (operatorname != null && isAppropriateType(uexpr.getOperand())) {
						CodanArguments ca = new CodanArguments(operatorname, Messages.MissingOperatorChecker_3 + operatorname + Messages.MissingOperatorChecker_4, ":operator"); //$NON-NLS-1$
						reportProblem(ERR_ID_OperatorResolutionProblem_HSR,	expression, ca.toArray());
						return PROCESS_SKIP;
					}
				}
			} else if (expression instanceof ICPPASTBinaryExpression) {
				ICPPASTBinaryExpression binex = (ICPPASTBinaryExpression) expression;
				IASTImplicitName[] inames = binex.getImplicitNames();
				if (!operatorFound(inames)) {
					if (isAppropriateType(binex.getOperand1())) {
						OverloadableOperator operator = OverloadableOperator.fromBinaryExpression(binex);
						String operatorname = new String(operator.toCharArray()).replaceAll("operator ", ""); //$NON-NLS-1$ //$NON-NLS-2$

						String typename = expression.getExpressionType().toString();
						CodanArguments ca = new CodanArguments(operatorname, Messages.MissingOperatorChecker_8 + operatorname + Messages.MissingOperatorChecker_9 + typename, ":operator"); //$NON-NLS-1$
						reportProblem(ERR_ID_OperatorResolutionProblem_HSR, binex, ca.toArray());
						return PROCESS_SKIP;
					}
				}
			}
			return PROCESS_CONTINUE;
		}

		private boolean implicitlyAvailableOperation(OverloadableOperator operator, CPPASTUnaryExpression uexpr) {
			IASTExpression operand = uexpr.getOperand();
			if(OverloadableOperator.NOT.compareTo(operator) == 0 && (operand.getExpressionType() instanceof IPointerType)){
				return true;
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

		private boolean operatorToSkip(OverloadableOperator operator) {
			switch (operator) {
			case AMPER:
				return true;
			}
			return false;
		}

		private boolean isCuteASSERTEQUALS(IASTExpression expression,
				OverloadableOperator operator) {
			if (operator == null) {
				return true;
			}
			return false;
		}


		private boolean isCuteAssert(IASTExpression expression,
				OverloadableOperator operator) {
			return operator.equals(OverloadableOperator.NOT) && expression.getParent() instanceof ICPPASTIfStatement;
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

		private IASTIdExpression getLastIDExpression(IASTNode selectedNode) {
			calculateLastIDExpression(selectedNode);
			return possibleresult;
		}

		//TODO: remove duplicated -> creatememberfunctionrefactoring
		private IASTIdExpression possibleresult = null;

		private void calculateLastIDExpression(IASTNode selectedNode) {
			if (selectedNode == null) {
				return;
			}
			for(IASTNode child: selectedNode.getChildren()) {
				if (child instanceof IASTIdExpression) {
					possibleresult = (IASTIdExpression) child;
				}
				calculateLastIDExpression(child);
			}
		}
	}
}
