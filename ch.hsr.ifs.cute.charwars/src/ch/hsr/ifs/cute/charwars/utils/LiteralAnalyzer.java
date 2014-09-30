package ch.hsr.ifs.cute.charwars.utils;

import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.charwars.constants.Constants;

public class LiteralAnalyzer {
	public static boolean isString(IASTNode node) {
		return getKind(node) == IASTLiteralExpression.lk_string_literal;
	}
	
	public static boolean isInteger(IASTNode node, int value) {
		return getKind(node) == IASTLiteralExpression.lk_integer_constant && 
			   getValue(node).equals(String.valueOf(value));
	}
	
	public static boolean isInteger(IASTNode node) {
		return getKind(node) == IASTLiteralExpression.lk_integer_constant;
	}
	
	public static boolean isZero(IASTNode node) {
		return isInteger(node, 0);
	}
	
	public static boolean isNullExpression(IASTNode node) {
		String literalValue = getValue(node);
		return Constants.NULL_VALUES.contains(literalValue);
	}
	
	private static int getKind(IASTNode node) {
		if(node instanceof IASTLiteralExpression) {
			IASTLiteralExpression literal = (IASTLiteralExpression)node;
			return literal.getKind();
		}
		return -1;
	}
	
	private static String getValue(IASTNode node) {
		if(node instanceof IASTLiteralExpression) {
			IASTLiteralExpression literal = (IASTLiteralExpression)node;
			return String.valueOf(literal.getValue());
		}
		return null;
	}
}
