/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
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
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTConstructorInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTPointer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReferenceOperator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPArrayType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.cdt.internal.ui.refactoring.togglefunction.NotSupportedException;
import org.eclipse.cdt.internal.ui.refactoring.togglefunction.ToggleNodeHelper;

@SuppressWarnings("restriction")
public class ParameterHelper {

	private static final String STD_STRING = "std::string"; //$NON-NLS-1$

	public static void addTo(IASTFunctionCallExpression caller, ICPPASTFunctionDeclarator decl) {
		HashMap<String, Boolean> used = new HashMap<String, Boolean>();
		ArrayList<ICPPASTParameterDeclaration> parameters = getParameterFrom(caller, used);
		addParametersToDeclarator(decl, parameters);
	}

	public static void addTo(CPPASTDeclarator declarator, ICPPASTFunctionDeclarator decl) {
		HashMap<String, Boolean> used = new HashMap<String, Boolean>();
		ArrayList<ICPPASTParameterDeclaration> parameters = getParameterFrom(declarator, used);
		addParametersToDeclarator(decl, parameters);
	}

	private static void addParametersToDeclarator(ICPPASTFunctionDeclarator decl, ArrayList<ICPPASTParameterDeclaration> parameters) {
		for (ICPPASTParameterDeclaration parameter : parameters) {
			decl.addParameterDeclaration(parameter);
		}
	}

	public static ArrayList<ICPPASTParameterDeclaration> getParameterFrom(IASTFunctionCallExpression call, HashMap<String, Boolean> used) {
		IASTInitializerClause[] arguments = call.getArguments();
		return addArgumentsToList(arguments, used);
	}

	public static ArrayList<ICPPASTParameterDeclaration> getParameterFrom(CPPASTDeclarator declarator, HashMap<String, Boolean> used) {
		CPPASTConstructorInitializer initializer = (CPPASTConstructorInitializer) declarator.getInitializer();
		if (initializer != null) {
			return addArgumentsToList(initializer.getArguments(), used);
		}
		return new ArrayList<ICPPASTParameterDeclaration>();
	}

	private static ArrayList<ICPPASTParameterDeclaration> addArgumentsToList(IASTInitializerClause[] arguments, HashMap<String, Boolean> used) {
		ArrayList<ICPPASTParameterDeclaration> list = new ArrayList<ICPPASTParameterDeclaration>();
		for (IASTInitializerClause arg : arguments) {
			IASTExpression posId = TddHelper.getChildofType(arg, IASTExpression.class);
			if (posId == null) {
				System.err.println("Expression is null for " + arg.getRawSignature());
			} else if (posId instanceof CPPASTLiteralExpression) {
				list.add(createParamDeclFrom((CPPASTLiteralExpression) posId, used));
			} else if (posId instanceof CPPASTIdExpression) {
				list.add(createParamDeclFrom((CPPASTIdExpression) posId, used));
			} else {
				String nameHint = arg.getRawSignature().replaceAll("[\\P{Alpha}&&\\P{Digit}]", "");
				list.add(createParamDeclFrom(posId.getExpressionType(), nameHint, used));
			}

		}
		return list;
	}

	public static ICPPASTParameterDeclaration createParamDeclFrom(IType type, String nameHint, HashMap<String, Boolean> used ) {

		ICPPASTDeclSpecifier spec = createDeclSpecifier(type);

		IASTName parameterName = createParameterName(nameHint, used);
		ICPPASTDeclarator decl = getParameterDeclarator(parameterName, type);

		if (!makeLastPtrOpConst(decl)) {
			spec.setConst(true);
		}

		return CPPNodeFactory.getDefault().newParameterDeclaration(spec, decl);
	}

	public static ICPPASTParameterDeclaration createParamDeclFrom(IASTIdExpression idexpr, HashMap<String, Boolean> used) {
		IType type = TypeHelper.getTypeOf(idexpr);
		ICPPASTDeclSpecifier spec = createDeclSpecifier(type);

		String nameHint = new String(idexpr.getName().getSimpleID());
		IASTName parameterName = createParameterName(nameHint, used);
		IASTDeclarator declarator = getParameterDeclarator(parameterName, type);
		return CPPNodeFactory.getDefault().newParameterDeclaration(spec, declarator);
	}

	private static ICPPASTDeclSpecifier createDeclSpecifier(IType type) {
		ICPPASTDeclSpecifier spec = TypeHelper.getDeclarationSpecifierOfType(TypeHelper.windDownToRealType(type, true));
		if (!spec.isConst()) {
			spec.setConst(TypeHelper.hasConstPart(type));
		}
		spec.setVolatile(TypeHelper.hasVolatilePart(type));
		return spec;
	}

	public static ICPPASTParameterDeclaration createParamDeclFrom(IASTLiteralExpression litexpr, HashMap<String, Boolean> used) {
		ICPPASTDeclSpecifier spec;
		String fallBackVarName = null;
		if (TypeHelper.isThisPointer(litexpr)) {
			spec = handlethis(litexpr);
			IASTCompositeTypeSpecifier parentType = ToggleNodeHelper.findClassInAncestors(litexpr);
			fallBackVarName = new String(parentType.getName().getSimpleID());
		} else if (TypeHelper.isString(litexpr)) {
			spec = createAnonymousStringDeclSpecifier();
			// TODO: get this string out of here
			fallBackVarName = new String(STD_STRING).toLowerCase();
		} else {
			IType type = litexpr.getExpressionType();
			type = TypeHelper.windDownToRealType(type, true);
			fallBackVarName = getFallBackName(type);
			spec = TypeHelper.getDeclarationSpecifierOfType(type);

		}		
		spec.setConst(true);
		
		String newName = getParameterCharacter(fallBackVarName, used);
		used.put(newName, true);	
		IASTDeclarator declarator = getParameterDeclarator(new CPPASTName(newName.toCharArray()), litexpr.getExpressionType() );
		makeLastPtrOpConst(declarator);
		
		return CPPNodeFactory.getDefault().newParameterDeclaration(spec, declarator);
	}

	private static boolean makeLastPtrOpConst(IASTDeclarator declarator) {
		IASTPointerOperator[] ptrOperators = declarator.getPointerOperators();
		if (ptrOperators != null) {
			for(int i = ptrOperators.length - 1; i >= 0; i--){
				IASTPointerOperator currentPtrOp = ptrOperators[i];
				if(currentPtrOp instanceof IASTPointer){
					((IASTPointer)currentPtrOp).setConst(true);
					return true;
				}
			}
		}
		return false;
	}

	private static IASTName createParameterName(String nameHint, HashMap<String, Boolean> used) {
		String newName = getParameterName(used, nameHint);
		used.put(newName, true);
		return new CPPASTName(newName.toCharArray());
	}

	private static ICPPASTDeclSpecifier handlethis(IASTLiteralExpression lit) {
		IASTCompositeTypeSpecifier parentType = ToggleNodeHelper.findClassInAncestors(lit);
		if (parentType == null) {
			throw new NotSupportedException(Messages.ParameterHelper_1);
		}
		CPPASTNamedTypeSpecifier d = new CPPASTNamedTypeSpecifier();
		d.setName(parentType.getName().copy());

		return d;
	}

	static ICPPASTDeclSpecifier createAnonymousStringDeclSpecifier() {
		CPPASTNamedTypeSpecifier declspec = new CPPASTNamedTypeSpecifier();
		char[] typename = STD_STRING.toCharArray();
		declspec.setName(new CPPASTName(typename));
		declspec.setConst(true);
		return declspec;
	}

	private static ICPPASTDeclarator getParameterDeclarator(IASTName parameterName, IType type) {
		ICPPASTDeclarator paramDecl = assembleDeclarator(parameterName, type);

		paramDecl.addPointerOperator(new CPPASTReferenceOperator(false));
		return paramDecl;
	}

	private static ICPPASTDeclarator assembleDeclarator(IASTName parameterName, IType type) {
		ICPPASTDeclarator paramDecl;
		if (type instanceof IPointerType) {
			paramDecl = getPointerParameterDeclarator(parameterName, (IPointerType) type);
		} else {
			paramDecl = new CPPASTDeclarator(parameterName);
		}
		return paramDecl;
	}


	private static ICPPASTDeclarator getPointerParameterDeclarator(IASTName parameterName, IPointerType type) {
		IType pointedType = type.getType();
		ICPPASTDeclarator paramDecl = assembleDeclarator(parameterName, pointedType);

		CPPASTPointer ptrOperator = new CPPASTPointer();
		ptrOperator.setConst(type.isConst());
		ptrOperator.setVolatile(type.isVolatile());

		paramDecl.addPointerOperator(ptrOperator);
		return paramDecl;
	}

	private static String getParameterName(HashMap<String, Boolean> used, String hint) {
		String newName = hint;
		if (used.get(newName) != null) {
			newName = newName + 1;
		}
		while (used.get(newName) != null) {
			newName = newName.substring(0, newName.length() - 1) + (char) (newName.charAt(newName.length() - 1) + 1);
		}
		return newName;
	}

	private static String getParameterCharacter(String fallBackVarName, HashMap<String, Boolean> used) {
		String newName = new String(fallBackVarName.charAt(0) + "").toLowerCase(); //$NON-NLS-1$
		while (used.get(newName) != null) {
			newName = (char) (newName.charAt(0) + 1) + ""; //$NON-NLS-1$
		}
		return newName;
	}

	private static String getFallBackName(IType type) {
		if (type instanceof ITypedef) {
			return ASTTypeUtil.getQualifiedName((ICPPBinding) type).substring(0, 1);
		}
		if (type instanceof CPPClassInstance || type instanceof ICPPClassType) {
			return ASTTypeUtil.getType(type).toLowerCase();
		} else if (type instanceof CPPBasicType) {
			return ((CPPBasicType) type).getKind().toString().substring(1).toLowerCase();
		}
		return null;
	}

	// TODO: to parameterhelper
	public static void addEmptyIntParameter(ICPPASTFunctionDeclarator decl) {
		CPPASTParameterDeclaration paramdecl = new CPPASTParameterDeclaration();
		paramdecl.setDeclarator(new CPPASTDeclarator(new CPPASTName()));
		paramdecl.setDeclSpecifier(TypeHelper.getDefaultType());
		decl.addParameterDeclaration(paramdecl);
	}
}
