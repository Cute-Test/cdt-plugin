package ch.hsr.ifs.cute.charwars.asttools;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;

import ch.hsr.ifs.cute.charwars.constants.Function;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.utils.FunctionAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.LiteralAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.TypeAnalyzer;

public class DeclaratorAnalyzer {
	public static boolean isCString(IASTDeclarator declarator) {
		boolean hasCStringType = hasCStringType(declarator, true) || hasCStringType(declarator, false); 
		return hasCStringType && (hasStringLiteralAssignment(declarator) || hasStrdupAssignment(declarator));
	}
	
	public static boolean isCStringAlias(IASTDeclarator declarator) {
		boolean hasCStringType = hasCStringType(declarator, true) || hasCStringType(declarator, false); 
		return hasCStringType && hasCStringAssignment(declarator);
	}
	
	public static boolean hasCStringAssignment(IASTDeclarator declarator) {
		IASTInitializer initializer = declarator.getInitializer();
		if(initializer instanceof IASTEqualsInitializer) {
			IASTEqualsInitializer equalsInitializer = (IASTEqualsInitializer)initializer;
			IASTInitializerClause initializerClause = equalsInitializer.getInitializerClause();
			boolean isConversionToCharPointer = ASTAnalyzer.isConversionToCharPointer(initializerClause, true);
			if(isConversionToCharPointer) {
				IASTFunctionCallExpression cstrCall = (IASTFunctionCallExpression)initializerClause;
				IASTFieldReference fieldReference = (IASTFieldReference)cstrCall.getFunctionNameExpression();
				IASTExpression fieldOwner = fieldReference.getFieldOwner();
				return TypeAnalyzer.isStdStringType(((IASTExpression)fieldOwner).getExpressionType());
			}
		}
		return false;
	}
	
	public static boolean hasCStringType(IASTDeclarator declarator, boolean isConst) {
		IASTNode parent = declarator.getParent();
		IASTDeclSpecifier declSpecifier = null;
		
		if(parent instanceof IASTSimpleDeclaration) {
			declSpecifier = ((IASTSimpleDeclaration)parent).getDeclSpecifier(); 
		}
		else if(parent instanceof ICPPASTParameterDeclaration) {
			declSpecifier = ((ICPPASTParameterDeclaration)parent).getDeclSpecifier();
		}
		
		if(declSpecifier instanceof IASTSimpleDeclSpecifier) {
			IASTSimpleDeclSpecifier simpleDeclSpecifier = (IASTSimpleDeclSpecifier)declSpecifier;
			int type = simpleDeclSpecifier.getType();
			boolean isValidType = type == IASTSimpleDeclSpecifier.t_char || 
								  type == IASTSimpleDeclSpecifier.t_wchar_t ||
								  type == IASTSimpleDeclSpecifier.t_char16_t ||
								  type == IASTSimpleDeclSpecifier.t_char32_t;
			return isValidType && isArrayXorPointer(declarator) && isConst == simpleDeclSpecifier.isConst();
		}
		return false;
	}
	
	public static boolean hasVoidType(IASTDeclSpecifier declSpecifier) {
		if(declSpecifier instanceof IASTSimpleDeclSpecifier) {
			IASTSimpleDeclSpecifier simpleDeclSpecifier = (IASTSimpleDeclSpecifier)declSpecifier;
			int type = simpleDeclSpecifier.getType();
			return type == IASTSimpleDeclSpecifier.t_void;
		}
		return false;
	}
	
	public static boolean isArray(IASTDeclarator declarator) {
		return declarator instanceof IASTArrayDeclarator;
	}
	
	private static boolean isArrayXorPointer(IASTDeclarator declarator) {
		return isArray(declarator) ^ isPointer(declarator);
	}
	
	public static boolean isPointer(IASTDeclarator declarator) {
		int numberOfPointers = 0;
		for(IASTPointerOperator po : declarator.getPointerOperators()) {
			if(po instanceof IASTPointer) {
				numberOfPointers++;
			}
		}
		return numberOfPointers == 1;
	}
	
	private static boolean hasStringLiteralAssignment(IASTDeclarator declarator) {
		IASTInitializerClause initializerClause = getInitializerClause(declarator);
		if(initializerClause != null) {
			return LiteralAnalyzer.isString(initializerClause);
		}
		return false;
	}
	
	public static boolean hasStrdupAssignment(IASTDeclarator declarator) {
		IASTInitializerClause initializerClause = getInitializerClause(declarator);
		if(initializerClause != null) {
			return FunctionAnalyzer.isCallToFunction(initializerClause, Function.STRDUP);
		}
		return false;
	}
	
	public static IASTInitializerClause getInitializerClause(IASTDeclarator declarator) {
		IASTInitializer initializer = declarator.getInitializer();
		if(initializer != null && initializer instanceof IASTEqualsInitializer) {
			IASTEqualsInitializer equalsInitializer = (IASTEqualsInitializer)initializer;
			return equalsInitializer.getInitializerClause();
		}
		return null;
	}
	
	public static String getStringReplacementType(IASTSimpleDeclSpecifier simpleDeclSpecifier) {
		switch(simpleDeclSpecifier.getType()) {
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
