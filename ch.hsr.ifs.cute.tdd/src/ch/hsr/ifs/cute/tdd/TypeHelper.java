/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBaseDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNamespace;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariable;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.IPDOMCPPClassType;
import org.eclipse.cdt.internal.ui.refactoring.NodeContainer;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.cdt.internal.ui.refactoring.togglefunction.ToggleNodeHelper;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.TextSelection;

import ch.hsr.ifs.cute.tdd.createfunction.PossibleReturnTypeFindVisitor;

public class TypeHelper {

	public static IType getTypeOf(IASTInitializerClause clause) {
		if (clause instanceof IASTInitializerList) {
			IASTInitializerClause[] clauses = ((IASTInitializerList) clause).getClauses();
			if (clauses.length > 0) {
				return getTypeOf(clauses[0]);
			}
			// Cannot fetch information about literal expressions inside initializer
			// lists because CModelBuilder2 sets parseFlags to
			// ITranslationUnit.AST_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS
			// -> Assume int as default
			return new CPPBasicType(Kind.eInt, 0);
		}
		if (clause instanceof IASTIdExpression) {
			IASTName xname = ((IASTIdExpression) clause).getName();
			IBinding binding = xname.resolveBinding();
			if (binding instanceof IVariable) {
				return ((IVariable) binding).getType();
			}
			if (binding instanceof IFunction) {
				return ((IFunction) binding).getType().getReturnType();
			}
			if (binding instanceof ITypedef) {
				return (ITypedef) binding;
			}
		}
		if (clause instanceof IASTExpression) {
			return ((IASTExpression) clause).getExpressionType();
		}
		return new CPPBasicType(Kind.eInt, 0);
	}

	public static ICPPASTDeclSpecifier getDeclarationSpecifierOfType(IType type) {
		if (type instanceof ICPPBinding) {
			return handleType((ICPPBinding) type);
		} else if (type instanceof IBasicType) {
			return handleType((IBasicType) type);
		} else { // not defined variables
			return defaultType();
		}
	}

	private static ICPPASTDeclSpecifier handleType(ICPPBinding type) {
		String typename = ASTTypeUtil.getQualifiedName((ICPPBinding) type);
		CPPASTNamedTypeSpecifier declspec = new CPPASTNamedTypeSpecifier(new CPPASTName(typename.toCharArray()));
		return declspec;
	}

	private static ICPPASTDeclSpecifier handleType(IBasicType type) {
		CPPASTSimpleDeclSpecifier simpleDeclSpec = new CPPASTSimpleDeclSpecifier();
		simpleDeclSpec.setType(((IBasicType) type).getKind());
		return simpleDeclSpec;
	}

	private static ICPPASTDeclSpecifier defaultType() {
		CPPASTBaseDeclSpecifier fallBackIntDeclSpec = TypeHelper.getDefaultType();
		fallBackIntDeclSpec.setConst(true);
		return fallBackIntDeclSpec;
	}

	static boolean hasConstPart(IType type) {
		if (type instanceof ITypeContainer) {
			if (type instanceof IQualifierType) {
				if (((IQualifierType) type).isConst()) {
					return true;
				}
			}
			return hasConstPart(((ITypeContainer) type).getType());
		}
		return false;
	}

	static boolean hasVolatilePart(IType type) {
		if (type instanceof ITypeContainer) {
			if (type instanceof IQualifierType) {
				if (((IQualifierType) type).isVolatile()) {
					return true;
				}
			}
			return hasVolatilePart(((ITypeContainer) type).getType());
		}
		return false;
	}

	public static boolean haveSameType(IASTInitializerClause argument, ICPPParameter parameter) {
		IType paramtype = windDownToRealType(parameter.getType(), true);
		if (argument instanceof IASTExpression) {
			IType argtype = windDownToRealType(((IASTExpression) argument).getExpressionType(), false);
			if (paramtype instanceof ICPPBasicType && argtype instanceof ICPPBasicType) {
				return true;
			}
			if (isString(argument) && isString(paramtype)) {
				return true;
			}
			return parameter.getType().isSameType(argtype);
		}
		return false;
	}

	public static boolean hasSameType(IASTDeclSpecifier one, IASTDeclSpecifier other) {
		if (one instanceof CPPASTNamedTypeSpecifier && other instanceof CPPASTNamedTypeSpecifier) {
			CPPASTNamedTypeSpecifier nameargspec = (CPPASTNamedTypeSpecifier) one;
			CPPASTNamedTypeSpecifier nameparspec = (CPPASTNamedTypeSpecifier) other;
			if (new String(nameargspec.getName().getSimpleID()).equals(new String(nameparspec.getName().getSimpleID()))) {
				return true;
			} else {
				return false;
			}
		} else if (one instanceof CPPASTSimpleDeclSpecifier) {
			if (other instanceof CPPASTSimpleDeclSpecifier) {
				CPPASTSimpleDeclSpecifier simpeargspec = (CPPASTSimpleDeclSpecifier) one;
				CPPASTSimpleDeclSpecifier simpeparspec = (CPPASTSimpleDeclSpecifier) other;
				if (simpeargspec.getType() == simpeparspec.getType()) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	public static IType windDownToRealType(IType type, boolean stopAtTypeDef) {
		if (type instanceof ITypeContainer) {
			if (stopAtTypeDef && type instanceof ITypedef) {
				return type;
			}
			type = ((ITypeContainer) type).getType();
			return windDownToRealType(type, stopAtTypeDef);
		}
		return type;
	}

	public static boolean isString(IType type) {
		return type instanceof IBinding && ((IBinding) type).getName().equals("string"); //$NON-NLS-1$
	}

	public static boolean isString(IASTInitializerClause argument) {
		if (argument instanceof IASTLiteralExpression) {
			return isString((CPPASTLiteralExpression) argument);
		}
		return false;
	}

	public static boolean isString(CPPASTLiteralExpression litexpr) {
		return litexpr.getKind() == ICPPASTLiteralExpression.lk_string_literal;
	}

	public static boolean isThisPointer(IASTLiteralExpression litexpr) {
		return litexpr.getKind() == ICPPASTLiteralExpression.lk_this;
	}

	public static ICPPASTCompositeTypeSpecifier getTypeDefinitionOfMember(IASTTranslationUnit unit, IASTName selectedNode, RefactoringASTCache astCache) {
		ICPPASTQualifiedName qName = getQualifiedNamePart(selectedNode);
		if (qName != null) {
			return handleQualifiedName(qName);
		}

		ICPPASTFieldReference fieldref = ToggleNodeHelper.getAncestorOfType(selectedNode, ICPPASTFieldReference.class);
		if (fieldref != null && fieldref.getFieldOwner().getExpressionType() instanceof IBinding) {
			IASTExpression owner = fieldref.getFieldOwner();
			IBinding expressionType = (IBinding) owner.getExpressionType();
			if (expressionType instanceof CPPClassType) {
				CPPClassType cType = (CPPClassType) expressionType;
				return cType.getCompositeTypeSpecifier();
			}
			if (expressionType instanceof IPDOMCPPClassType) {
				return findTypeDefinitionInIndex(unit, astCache, (IPDOMCPPClassType) expressionType);
			}
		} else {
			IASTSimpleDeclaration declaration = ToggleNodeHelper.getAncestorOfType(selectedNode, IASTSimpleDeclaration.class);
			if (declaration != null) {
				final IASTDeclSpecifier type = declaration.getDeclSpecifier();
				if (type instanceof ICPPASTCompositeTypeSpecifier) {
					return (ICPPASTCompositeTypeSpecifier) type;
				}
			}
		}
		return null;
	}

	public static ICPPASTCompositeTypeSpecifier getTypeDefinitionOfVariable(IASTTranslationUnit unit, IASTName selectedNode, RefactoringASTCache astCache) {
		ICPPASTQualifiedName qName = getQualifiedNamePart(selectedNode);
		if (qName != null) {
			selectedNode = qName;
		}
		IBinding b = selectedNode.resolveBinding();
		if (b instanceof CPPVariable) {
			CPPVariable var = (CPPVariable) b;
			IType type = var.getType();
			return getDefinitionOfType(unit, astCache, type);
		}
		return null;
	}

	private static ICPPASTCompositeTypeSpecifier findTypeDefinitionInIndex(IASTTranslationUnit unit, RefactoringASTCache astCache, ICPPClassType cType) {

		try {
			Set<IIndexFile> includedFiles = getIncludedFiles(unit, cType);

			IIndexName[] definitions = unit.getIndex().findDefinitions(cType);
			for (IIndexName def : definitions) {
				final IIndexFile file = def.getFile();
				if (includedFiles.contains(file)) {

					IPath defPath = new Path(def.getFileLocation().getFileName());
					ITranslationUnit tu = (ITranslationUnit) CCorePlugin.getDefault().getCoreModel().create(defPath);
					if (tu != null) {
						IASTTranslationUnit tuContainingDefinition = astCache.getAST(tu, new NullProgressMonitor());
						final IIndexBinding adaptedTypeBinding = tuContainingDefinition.getIndex().adaptBinding(cType);
						IASTName[] typeDefinitions = tuContainingDefinition.getDefinitionsInAST(adaptedTypeBinding);
						if (typeDefinitions.length > 0) {
							IBinding typeBinding = typeDefinitions[0].getBinding();
							if (typeBinding instanceof CPPClassType) {
								return ((CPPClassType) typeBinding).getCompositeTypeSpecifier();
							}
						}
					}
				}
			}
		} catch (CoreException e) {
		}
		return null;
	}

	private static Set<IIndexFile> getIncludedFiles(IASTTranslationUnit unit, ICPPClassType cType) throws CoreException {
		IIndexFileLocation ifl = IndexLocationFactory.getIFLExpensive(unit.getFilePath());
		final int cppLinkageID = cType.getLinkage().getLinkageID();
		IIndexFile astFile = unit.getIndex().getFile(cppLinkageID, ifl);

		Set<IIndexFile> includedFiles = new HashSet<IIndexFile>();

		IIndexInclude[] includes = unit.getIndex().findIncludes(astFile);
		for (IIndexInclude include : includes) {
			IIndexFile fileIncludedByInclude = unit.getIndex().getFile(cppLinkageID, include.getIncludesLocation());
			includedFiles.add(fileIncludedByInclude);
		}
		return includedFiles;
	}

	private static ICPPASTCompositeTypeSpecifier handleQualifiedName(ICPPASTQualifiedName qName) {
		IASTName[] nameParts = qName.getNames();
		if (nameParts.length >= 2) {
			IASTName containingScope = nameParts[nameParts.length - 2];
			IBinding scopeBinding = containingScope.resolveBinding();
			if (scopeBinding instanceof IType) {
				IType bareType = windDownToRealType((IType) scopeBinding, false);
				if (bareType instanceof CPPClassType) {
					return ((CPPClassType) bareType).getCompositeTypeSpecifier();
				}
			}
		}
		return null;
	}

	private static ICPPASTQualifiedName getQualifiedNamePart(IASTName selectedNode) {
		IASTNode parent = selectedNode.getParent();
		if (parent instanceof ICPPASTQualifiedName) {
			return (ICPPASTQualifiedName) parent;
		}
		return null;
	}

	public static IASTNode getTypeDefinitonOfName(IASTTranslationUnit localunit, final String typename, RefactoringASTCache astCache) {
		try {
			return TypeHelper.checkIndexBindingForType(typename, localunit, astCache.getIndex(), astCache);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static IASTNode checkIndexBindingForType(final String typename, IASTTranslationUnit unit, IIndex index, RefactoringASTCache astCache) throws CoreException {
		IIndexBinding[] allBindings = index.findBindings(typename.toCharArray(), false, new IndexFilter() {
		}, new NullProgressMonitor());
		for (IIndexBinding binding : allBindings) {
			IIndexName[] names = index.findNames(binding, IIndex.FIND_ALL_OCCURRENCES);
			for (IIndexName name : names) {
				IASTTranslationUnit currentTu = unit;
				if (!name.getFileLocation().getFileName().equals(currentTu.getFileLocation().getFileName())) {
					IPath path = new Path(name.getFileLocation().getFileName());
					ITranslationUnit tu = (ITranslationUnit) CCorePlugin.getDefault().getCoreModel().create(path); // TODO: Review: assignment to
																													// parameter, and this shouts for
																													// an else case
					if (tu != null) {
						currentTu = astCache.getAST(tu, new NullProgressMonitor());
					}
				}
				IASTNode typeSpec = searchTypeInUnit(typename, currentTu);
				if (typeSpec != null) {
					return typeSpec;
				}
			}
		}
		return null;
	}

	private static IASTNode searchTypeInUnit(final String typeToLookFor, IASTTranslationUnit tu) {
		final NodeContainer container = new NodeContainer();
		tu.accept(new ASTVisitor() {
			{
				shouldVisitDeclarations = true;
				shouldVisitNamespaces = true;
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				if (declaration instanceof CPPASTSimpleDeclaration) {
					CPPASTSimpleDeclaration spec = ((CPPASTSimpleDeclaration) declaration);
					if (spec.getDeclSpecifier() instanceof CPPASTCompositeTypeSpecifier) {
						CPPASTCompositeTypeSpecifier foundType = (CPPASTCompositeTypeSpecifier) spec.getDeclSpecifier();
						String foundTypeName = foundType.getName().toString();
						if (typeToLookFor.equals(foundTypeName)) {
							container.add(foundType);
							return PROCESS_ABORT;
						}
					}
				}
				return PROCESS_CONTINUE;
			}

			@Override
			public int visit(ICPPASTNamespaceDefinition nsdef) {
				String nsName = new String(nsdef.getName().getSimpleID());
				if (nsName.equals(typeToLookFor)) {
					container.add(nsdef);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
		if (container.size() > 0) {
			return container.getNodesToWrite().get(0);
		}
		return null;
	}

	public static CPPASTBaseDeclSpecifier getDefaultType() {
		CPPASTBaseDeclSpecifier spec = new CPPASTSimpleDeclSpecifier();
		((IASTSimpleDeclSpecifier) spec).setType(IASTSimpleDeclSpecifier.t_int);
		return spec;
	}

	public static CPPASTBaseDeclSpecifier getDefaultReturnType() {
		CPPASTBaseDeclSpecifier spec = new CPPASTSimpleDeclSpecifier();
		((IASTSimpleDeclSpecifier) spec).setType(IASTSimpleDeclSpecifier.t_void);
		return spec;
	}

	public static CPPASTBaseDeclSpecifier findTypeInAst(IASTTranslationUnit localunit, TextSelection selection) {
		final NodeContainer c = new NodeContainer();
		PossibleReturnTypeFindVisitor finder = new PossibleReturnTypeFindVisitor(selection, c);
		localunit.accept(finder);

		if (finder.hasFound()) {
			IASTNode node = finder.getType();
			if (node != null && node instanceof CPPASTSimpleDeclSpecifier) {
				return (CPPASTSimpleDeclSpecifier) node.copy(CopyStyle.withLocations);
			} else if (node instanceof CPPASTNamedTypeSpecifier) {
				return (CPPASTNamedTypeSpecifier) node.copy(CopyStyle.withLocations);
			}
		}
		return null;
	}

	public static IASTNode getMemberOwner(IASTTranslationUnit localunit, IASTName selectedNode, RefactoringASTCache astCache) {
		IBinding binding = selectedNode.resolveBinding();

		if (binding != null) {
			IBinding owner = binding.getOwner();

			if (owner instanceof CPPNamespace) {
				ICPPNamespaceScope scope = ((CPPNamespace) owner).getNamespaceScope();
				if (scope instanceof CPPScope) {
					return ((CPPScope) scope).getPhysicalNode();
				}
			}
		}
		return getTypeDefinitionOfMember(localunit, selectedNode, astCache);
	}

	public static ICPPASTCompositeTypeSpecifier getTypeOfMember(IASTTranslationUnit unit, IASTName selectedNode, RefactoringASTCache astCache) {
		IBinding b = selectedNode.resolveBinding();
		if (b instanceof ICPPMember) {
			ICPPMember var = (ICPPMember) b;
			IType type = var.getClassOwner();
			return getDefinitionOfType(unit, astCache, type);
		}
		return null;
	}

	private static ICPPASTCompositeTypeSpecifier getDefinitionOfType(IASTTranslationUnit unit, RefactoringASTCache astCache, IType type) {
		type = TypeHelper.windDownToRealType(type, false);
		if (type instanceof ICPPClassSpecialization) {
			type = ((ICPPClassSpecialization) type).getSpecializedBinding();
		}
		if (type instanceof CPPClassTemplate) {
			return ((CPPClassTemplate) type).getCompositeTypeSpecifier();
		}
		if (type instanceof CPPClassType) {
			return ((CPPClassType) type).getCompositeTypeSpecifier();
		}
		if (type instanceof ICPPClassType) {
			return findTypeDefinitionInIndex(unit, astCache, (ICPPClassType) type);
		}
		return null;
	}

}
