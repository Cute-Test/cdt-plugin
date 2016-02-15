package ch.hsr.ifs.cute.constificator.core.deciders.util;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;

public class Constants {

	public static final Integer[] MODIFYING_BINARY_OPERATORS = {
			ICPPASTBinaryExpression.op_assign,
			ICPPASTBinaryExpression.op_binaryAndAssign,
			ICPPASTBinaryExpression.op_binaryOrAssign,
			ICPPASTBinaryExpression.op_binaryXorAssign,
			ICPPASTBinaryExpression.op_divideAssign,
			ICPPASTBinaryExpression.op_minusAssign,
			ICPPASTBinaryExpression.op_moduloAssign,
			ICPPASTBinaryExpression.op_multiplyAssign,
			ICPPASTBinaryExpression.op_plusAssign,
			ICPPASTBinaryExpression.op_shiftLeftAssign,
			ICPPASTBinaryExpression.op_shiftRightAssign
			};

	public static final Integer[] MODIFYING_UNARY_OPERATORS = {
			ICPPASTUnaryExpression.op_prefixIncr,
			ICPPASTUnaryExpression.op_prefixDecr,
			ICPPASTUnaryExpression.op_postFixIncr,
			ICPPASTUnaryExpression.op_postFixDecr
			};

}
