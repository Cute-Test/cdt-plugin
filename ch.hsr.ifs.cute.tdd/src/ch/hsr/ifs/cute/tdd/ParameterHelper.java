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
import java.util.Iterator;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
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
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReferenceOperator;
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

	private static void addParametersToDeclarator(ICPPASTFunctionDeclarator decl,
			ArrayList<ICPPASTParameterDeclaration> parameters) {
		for (ICPPASTParameterDeclaration parameter: parameters) {
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
		for (IASTInitializerClause arg: arguments) {
			CPPASTIdExpression posId = TddHelper.getChildofType(arg, CPPASTIdExpression.class);
			if (posId != null) {
				arg = posId;
			}
			if (arg instanceof CPPASTLiteralExpression) {
				list.add(createParamDeclFrom((CPPASTLiteralExpression) arg, used));
			}
			if (arg instanceof CPPASTIdExpression) {
				list.add(createParamDeclFrom((CPPASTIdExpression) arg, used));
			}
		}
		return list;
	}

	public static boolean haveSameParameter(IASTInitializer initializer, CPPASTFunctionDeclarator declarator) {
		if (initializer == null && declarator.getParameters().length == 0) {
			return true;
		}
		ArrayList<ICPPASTParameterDeclaration> parameters = new ArrayList<ICPPASTParameterDeclaration>();
		for (ICPPASTParameterDeclaration parameter:  declarator.getParameters()) {
			parameters.add(parameter);
		}
		ArrayList<ICPPASTParameterDeclaration> arguments = getParameterFrom((CPPASTDeclarator)initializer.getParent(), new HashMap<String, Boolean>());
		Iterator<ICPPASTParameterDeclaration> argit = arguments.iterator();
		Iterator<ICPPASTParameterDeclaration> parit = parameters.iterator();
		while(argit.hasNext() && parit.hasNext()) {
			ICPPASTParameterDeclaration argument = argit.next();
			ICPPASTParameterDeclaration parameter = parit.next();
			if (!hasEqualTypeAs(argument, parameter)) {
				return false;
			}
		}
		return true;
	}

	public static boolean hasEqualTypeAs(ICPPASTParameterDeclaration first, ICPPASTParameterDeclaration second) {
		return TypeHelper.hasSameType(first.getDeclSpecifier(), second.getDeclSpecifier());
	}

	public static ICPPASTParameterDeclaration createParamDeclFrom(IASTLiteralExpression litexpr, HashMap<String, Boolean> used) {
		ICPPASTDeclSpecifier spec;
		String fallBackVarName = null;
		if (TypeHelper.isThisPointer(litexpr)) {
			spec = handlethis(litexpr);
			IASTCompositeTypeSpecifier parentType = ToggleNodeHelper.findClassInAncestors(litexpr);
			fallBackVarName = new String(parentType.getName().getSimpleID());
		}
		else if (TypeHelper.isString(litexpr)) {
			spec = handlestring();
			//TODO: get this string out of here
			fallBackVarName = new String(STD_STRING).toLowerCase();
		} else {
			IType type = litexpr.getExpressionType();
			boolean needsConst = true;
			if (TypeHelper.hasQualifierType(type)) {
				needsConst = true;
			}
			type = TypeHelper.windDownToRealType(type, true);
			fallBackVarName = getFallBackName(type);
			spec = TypeHelper.getDeclarationSpecifierOfType(type);
			if (!spec.isConst()) {
				spec.setConst(needsConst);
			}
		}
		IASTDeclarator declarator = getParameterDeclarator(fallBackVarName, used);
		return CPPNodeFactory.getDefault().newParameterDeclaration(spec, declarator);
	}

	public static ICPPASTParameterDeclaration createParamDeclFrom(IASTIdExpression idexpr, HashMap<String, Boolean> used) {
		boolean needsConst = false;
		IType type = TypeHelper.getTypeOf(idexpr);
		if (TypeHelper.hasQualifierType(type)) {
			needsConst = true;
		}
		type = TypeHelper.windDownToRealType(type, true);
		ICPPASTDeclSpecifier spec = TypeHelper.getDeclarationSpecifierOfType(type);
		if (!spec.isConst()) {
			spec.setConst(needsConst);
		}
		IASTDeclarator declarator = getParameterDeclarator(idexpr, used);
		return CPPNodeFactory.getDefault().newParameterDeclaration(spec, declarator);
	}

	private static ICPPASTDeclSpecifier handlethis(IASTLiteralExpression lit) {
		IASTCompositeTypeSpecifier parentType = ToggleNodeHelper.findClassInAncestors(lit);
		if (parentType == null) {
			throw new NotSupportedException(Messages.ParameterHelper_1);
		}
		CPPASTNamedTypeSpecifier d = new CPPASTNamedTypeSpecifier();
		d.setName(parentType.getName().copy());
		d.setConst(true);
		return d;
	}

	static ICPPASTDeclSpecifier handlestring() {
		CPPASTNamedTypeSpecifier declspec = new CPPASTNamedTypeSpecifier();
		char[] typename = STD_STRING.toCharArray();
		declspec.setName(new CPPASTName(typename));
		declspec.setConst(true);
		return declspec;
	}

	private static ICPPASTDeclarator getParameterDeclarator(String fallBackName, HashMap<String, Boolean> used) {
		String newName = getParameterCharacter(fallBackName, used);
		used.put(newName, true);
		CPPASTDeclarator d = new CPPASTDeclarator(new CPPASTName(newName.toCharArray()));
		d.addPointerOperator(new CPPASTReferenceOperator(false));
		return d;
	}

	private static ICPPASTDeclarator getParameterDeclarator(IASTIdExpression node, HashMap<String, Boolean> used) {
		String newName = getParameterName(used, node);
		used.put(newName, true);
		CPPASTDeclarator d = new CPPASTDeclarator(new CPPASTName(newName.toCharArray()));
		d.addPointerOperator(new CPPASTReferenceOperator(false));
		return d;
	}

	private static String getParameterName(HashMap<String, Boolean> used, IASTIdExpression node) {
		String newName = new String(node.getName().getSimpleID());
		if (used.get(newName) != null) {
			newName = newName + 1;
		}
		while (used.get(newName) != null) {
			newName = newName.substring(0, newName.length() - 1)
					+ (char) (newName.charAt(newName.length() - 1) + 1);
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
			return ASTTypeUtil.getQualifiedName((ICPPBinding) type).substring(0,1);
		}
		if (type instanceof CPPClassInstance || type instanceof ICPPClassType) {
			return ASTTypeUtil.getType(type).toLowerCase();
		}
		else if(type instanceof CPPBasicType) {
			return ((CPPBasicType) type).getKind().toString().substring(1).toLowerCase();
		}
		return null;
	}

	//TODO: to parameterhelper
	public static void addEmptyIntParameter(ICPPASTFunctionDeclarator decl) {
		CPPASTParameterDeclaration paramdecl = new CPPASTParameterDeclaration();
		paramdecl.setDeclarator(new CPPASTDeclarator(new CPPASTName()));
		paramdecl.setDeclSpecifier(TypeHelper.getDefaultType());
		decl.addParameterDeclaration(paramdecl);
	}
}
