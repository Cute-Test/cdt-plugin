package ch.hsr.ifs.cute.charwars.asttools;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;

import ch.hsr.ifs.cute.charwars.constants.Function;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.utils.analyzers.DeclaratorTypeAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.analyzers.FunctionAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.analyzers.LiteralAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.analyzers.TypeAnalyzer;

public class DeclaratorAnalyzer {
	public static boolean isCString(IASTDeclarator declarator) {
		final boolean hasCStringType = DeclaratorTypeAnalyzer.hasCStringType(declarator);
		return hasCStringType && (hasStringLiteralAssignment(declarator) || hasStrdupAssignment(declarator));
	}

	public static boolean isCStringAlias(IASTDeclarator declarator) {
		final boolean hasCStringType = DeclaratorTypeAnalyzer.hasCStringType(declarator);
		return hasCStringType && (hasCStringAssignment(declarator) || hasOffsettedCStringAssignment(declarator));
	}

	private static boolean hasCStringAssignment(IASTDeclarator declarator) {
		final IASTInitializerClause initializerClause = getInitializerClause(declarator);
		if(initializerClause != null) {
			final boolean isConversionToCharPointer = ASTAnalyzer.isConversionToCharPointer(initializerClause, true);
			if(isConversionToCharPointer) {
				final IASTFunctionCallExpression cstrCall = (IASTFunctionCallExpression)initializerClause;
				final IASTFieldReference fieldReference = (IASTFieldReference)cstrCall.getFunctionNameExpression();
				final IASTExpression fieldOwner = fieldReference.getFieldOwner();
				return TypeAnalyzer.isStdStringType(fieldOwner.getExpressionType());
			}
		}
		return false;
	}

	private static boolean hasOffsettedCStringAssignment(IASTDeclarator declarator) {
		final IASTInitializerClause initializerClause = getInitializerClause(declarator);
		if(initializerClause instanceof IASTExpression) {
			final IASTExpression expr = (IASTExpression)initializerClause;
			return ASTAnalyzer.isOffsettedCString(expr);
		}
		return false;
	}

	private static boolean hasStringLiteralAssignment(IASTDeclarator declarator) {
		final IASTInitializerClause initializerClause = getInitializerClause(declarator);
		return LiteralAnalyzer.isString(initializerClause);
	}

	public static boolean hasStrdupAssignment(IASTDeclarator declarator) {
		final IASTInitializerClause initializerClause = getInitializerClause(declarator);
		return FunctionAnalyzer.isCallToFunction(initializerClause, Function.STRDUP);
	}

	public static IASTInitializerClause getInitializerClause(IASTDeclarator declarator) {
		final IASTInitializer initializer = declarator.getInitializer();
		if(initializer instanceof IASTEqualsInitializer) {
			final IASTEqualsInitializer equalsInitializer = (IASTEqualsInitializer)initializer;
			return equalsInitializer.getInitializerClause();
		}
		return null;
	}

	public static String getStringReplacementType(IASTDeclarator declarator) {
		final IASTSimpleDeclSpecifier ds = DeclaratorTypeAnalyzer.getDeclSpecifier(declarator);
		if(ds == null) {
			return null;
		}

		switch(ds.getType()) {
		case IASTSimpleDeclSpecifier.t_wchar_t:
			return StdString.STD_WSTRING;
		case IASTSimpleDeclSpecifier.t_char16_t:
			return StdString.STD_U16STRING;
		case IASTSimpleDeclSpecifier.t_char32_t:
			return StdString.STD_U32STRING;
		default:
			return StdString.STD_STRING;
		}
	}
}
