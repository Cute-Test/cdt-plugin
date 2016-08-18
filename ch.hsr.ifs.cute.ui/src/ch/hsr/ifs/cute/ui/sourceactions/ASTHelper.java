/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.sourceactions;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class ASTHelper {
	private static final String EMPTY_STRING = "";

	public static String getClassStructName(IASTSimpleDeclaration simpleDeclaration) {
		IASTDeclSpecifier declspecifier = simpleDeclaration.getDeclSpecifier();
		if (declspecifier != null && declspecifier instanceof ICPPASTCompositeTypeSpecifier) {
			return ((ICPPASTCompositeTypeSpecifier) declspecifier).getName().toString();
		}
		return EMPTY_STRING;
	}

	public static String getMethodName(IASTDeclaration declaration) {
		if (declaration instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition fd = (IASTFunctionDefinition) declaration;
			IASTFunctionDeclarator fdd = fd.getDeclarator();
			String fname = fdd.getName().toString();
			return fname;
		} else if (declaration instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration sd = (IASTSimpleDeclaration) declaration;
			IASTDeclarator sdd[] = sd.getDeclarators();
			if (sdd.length == 0)
				return EMPTY_STRING;
			String sname = sdd[0].getName().toString();
			return sname;
		}
		return EMPTY_STRING;
	}

	public static ArrayList<IASTDeclaration> getConstructors(IASTCompositeTypeSpecifier typeNode) {
		ArrayList<IASTDeclaration> result = new ArrayList<IASTDeclaration>();

		String className = typeNode.getName().toString();
		IASTDeclaration members[] = typeNode.getMembers();
		for (int i = 0; i < members.length; i++) {
			if (members[i] instanceof IASTFunctionDefinition) {
				IASTFunctionDefinition fd = (IASTFunctionDefinition) members[i];
				IASTFunctionDeclarator fdd = fd.getDeclarator();
				String fname = fdd.getName().toString();
				if (fname.equals(className))
					result.add(fd);
			} else if (members[i] instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration sd = (IASTSimpleDeclaration) members[i];
				IASTDeclarator sdd[] = sd.getDeclarators();
				if (sdd.length == 0)
					continue;
				String sname = sdd[0].getName().toString();
				if (sname.equals(className))
					result.add(sd);
			}
		}
		return result;
	}

	public static boolean haveParameters(IASTDeclaration i) {
		try {
			if (i instanceof IASTFunctionDefinition) {
				IASTFunctionDefinition fd = (IASTFunctionDefinition) i;
				ICPPASTFunctionDeclarator fdd = (ICPPASTFunctionDeclarator) fd.getDeclarator();
				IASTParameterDeclaration fpara[] = fdd.getParameters();
				if (fdd.takesVarArgs() || fpara.length > 0)
					return true;
			} else if (i instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration sd = (IASTSimpleDeclaration) i;
				IASTDeclarator sdd[] = sd.getDeclarators();

				for (int j = 0; j < sdd.length; j++) {
					if (!(sdd[j] instanceof ICPPASTFunctionDeclarator))
						continue;
					// insert test case
					ICPPASTFunctionDeclarator fd = (ICPPASTFunctionDeclarator) sdd[j];
					IASTParameterDeclaration fpara[] = fd.getParameters();
					if (fd.takesVarArgs() || fpara != null && fpara.length > 0)
						return true;
				}
			}
		} catch (ClassCastException cce) {
			return false;
		}
		return false;
	}

	public static boolean haveParameters(ArrayList<IASTDeclaration> al) {
		for (IASTDeclaration i : al) {
			if (haveParameters(i)) {
				return true;
			}
		}
		return false;
	}

	public static ArrayList<IASTDeclaration> getParameterlessMethods(ArrayList<IASTDeclaration> al) {
		ArrayList<IASTDeclaration> result = new ArrayList<IASTDeclaration>();

		for (IASTDeclaration i : al) {
			if (!haveParameters(i))
				result.add(i);
		}
		return result;
	}

	public static boolean isVoid(IASTDeclaration i) {
		boolean result = false;

		try {
			if (i instanceof IASTFunctionDefinition) {
				IASTFunctionDefinition fd = (IASTFunctionDefinition) i;
				IASTSimpleDeclSpecifier specifier = (IASTSimpleDeclSpecifier) fd.getDeclSpecifier();
				if (specifier.getType() == IASTSimpleDeclSpecifier.t_void)
					result = true;
			} else if (i instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration sd = (IASTSimpleDeclaration) i;
				// CPPASTNamedTypeSpecifier returned via getDeclSpecifier?
				IASTSimpleDeclSpecifier specifier = (IASTSimpleDeclSpecifier) sd.getDeclSpecifier();
				if (specifier.getType() == IASTSimpleDeclSpecifier.t_void)
					result = true;
			}
		} catch (ClassCastException cce) {
			return false;
		}

		return result;
	}

	public static ArrayList<IASTDeclaration> removeVoidMethods(ArrayList<IASTDeclaration> member) {
		return scanVoidMethods(member, false);
	}

	public static ArrayList<IASTDeclaration> getVoidMethods(ArrayList<IASTDeclaration> member) {
		return scanVoidMethods(member, true);
	}

	private static ArrayList<IASTDeclaration> scanVoidMethods(ArrayList<IASTDeclaration> member, boolean flag) {
		ArrayList<IASTDeclaration> result = new ArrayList<IASTDeclaration>();

		for (IASTDeclaration simpleDeclaration : member) {
			if (isVoid(simpleDeclaration) == flag)
				result.add(simpleDeclaration);
		}

		return result;
	}

	public static ArrayList<IASTDeclaration> getPublicMethods(IASTSimpleDeclaration cppClass) {

		final ArrayList<IASTDeclaration> result = new ArrayList<IASTDeclaration>();
		final IASTDeclSpecifier declspecifier = cppClass.getDeclSpecifier();

		if (declspecifier != null && declspecifier instanceof ICPPASTCompositeTypeSpecifier) {
			final ICPPASTCompositeTypeSpecifier cts = (ICPPASTCompositeTypeSpecifier) declspecifier;
			final String className = cts.getName().toString();
			boolean ispublicVisibility = false;
			if (cts.getKey() == ICPPASTCompositeTypeSpecifier.k_struct)
				ispublicVisibility = true;
			else if (cts.getKey() == ICPPASTCompositeTypeSpecifier.k_class)
				ispublicVisibility = false;
			else {
				// TODO consider error handing
				return result;
			}

			final IASTDeclaration members[] = cts.getMembers();
			for (int i = 0; i < members.length; i++) {
				if (members[i] instanceof ICPPASTVisibilityLabel) {
					ispublicVisibility = changeVisibilityMode((ICPPASTVisibilityLabel) members[i]);
					continue;
				}

				if (!ispublicVisibility)
					continue;

				String methodName = EMPTY_STRING;
				if (members[i] instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration simpleDeclaration1 = (IASTSimpleDeclaration) members[i];
					IASTDeclarator declarator[] = simpleDeclaration1.getDeclarators();
					if (declarator != null && declarator.length > 0)
						methodName = declarator[0].getName().toString();

				} else if (members[i] instanceof IASTFunctionDefinition) {
					IASTFunctionDefinition funcdef = (IASTFunctionDefinition) members[i];
					IASTFunctionDeclarator funcdeclarator = funcdef.getDeclarator();
					methodName = funcdeclarator.getName().toString();
				}
				if (methodName.isEmpty())
					continue;
				if (className.equals(methodName))
					continue;// constructor

				result.add(members[i]);
			}
		}
		return result;
	}

	private static boolean changeVisibilityMode(ICPPASTVisibilityLabel node) {
		final int visbility = node.getVisibility();
		return visbility == ICPPASTVisibilityLabel.v_public;
	}

	public static ArrayList<IASTSimpleDeclaration> removeTemplateClasses(ArrayList<IASTSimpleDeclaration> cppClassStruct) {
		ArrayList<IASTSimpleDeclaration> result = new ArrayList<IASTSimpleDeclaration>();

		for (IASTSimpleDeclaration simpleDeclaration : cppClassStruct) {
			if (simpleDeclaration.getParent() instanceof ICPPASTTemplateDeclaration)
				continue;
			result.add(simpleDeclaration);
		}
		return result;
	}

	public static ArrayList<IASTDeclaration> getStaticMethods(ArrayList<IASTDeclaration> member) {
		ArrayList<IASTDeclaration> result = new ArrayList<IASTDeclaration>();

		for (IASTDeclaration m : member) {
			if (m instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration simpleDeclaration1 = (IASTSimpleDeclaration) m;
				IASTDeclSpecifier specifier = simpleDeclaration1.getDeclSpecifier();
				if (specifier.getStorageClass() == IASTDeclSpecifier.sc_static)
					result.add(m);
			} else if (m instanceof IASTFunctionDefinition) {
				IASTFunctionDefinition funcdef = (IASTFunctionDefinition) m;
				IASTDeclSpecifier specifier = funcdef.getDeclSpecifier();
				if (specifier.getStorageClass() == IASTDeclSpecifier.sc_static)
					result.add(m);
			}
		}
		return result;
	}

	public static ArrayList<IASTDeclaration> getNonStaticMethods(ArrayList<IASTDeclaration> member) {
		ArrayList<IASTDeclaration> result = new ArrayList<IASTDeclaration>();

		for (IASTDeclaration m : member) {
			IASTDeclSpecifier specifier = null;
			;
			if (m instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration simpleDeclaration1 = (IASTSimpleDeclaration) m;
				specifier = simpleDeclaration1.getDeclSpecifier();
			} else if (m instanceof IASTFunctionDefinition) {
				IASTFunctionDefinition funcdef = (IASTFunctionDefinition) m;
				specifier = funcdef.getDeclSpecifier();
			}
			if (specifier != null && specifier.getStorageClass() != IASTDeclSpecifier.sc_static)
				result.add(m);
		}

		return result;
	}

	// remove the primitives variables
	public static ArrayList<IASTSimpleDeclaration> getClassStructVariables(ArrayList<IASTSimpleDeclaration> variablesList) {
		ArrayList<IASTSimpleDeclaration> result = new ArrayList<IASTSimpleDeclaration>();

		for (IASTSimpleDeclaration i : variablesList) {
			if (i.getDeclSpecifier() instanceof ICPPASTNamedTypeSpecifier) {
				result.add(i);
			}
		}

		return result;
	}

	public static String getVariableName(IASTSimpleDeclaration variable) {
		IASTDeclarator declarators[] = variable.getDeclarators();
		if (declarators != null && declarators.length > 0) {
			return declarators[0].getName().toString();
		}
		return EMPTY_STRING;
	}

	public static boolean isUnion(IASTDeclaration variable) {
		if (variable instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) variable;
			IASTDeclSpecifier specifier = simpleDeclaration.getDeclSpecifier();
			if (specifier instanceof IASTCompositeTypeSpecifier) {
				IASTCompositeTypeSpecifier cts = (IASTCompositeTypeSpecifier) specifier;
				if (cts.getKey() == IASTCompositeTypeSpecifier.k_union)
					return true;
			}
		}
		return false;
	}

	public static ArrayList<IASTDeclaration> removeUnion(ArrayList<IASTDeclaration> variablesList) {
		ArrayList<IASTDeclaration> result = new ArrayList<IASTDeclaration>();

		for (IASTDeclaration i : variablesList) {
			if (!isUnion(i)) {
				result.add(i);
			}
		}

		return result;
	}

	public static boolean isOperator(IASTSimpleDeclaration variable) {
		if (getVariableName(variable).equals(Messages.getString("ASTHelper.Operator")))
			return true;
		return false;
	}

	public static boolean isOperator(IASTFunctionDefinition variable) {
		IASTFunctionDeclarator declarator = variable.getDeclarator();
		if (declarator.getName().toString().equals(Messages.getString("ASTHelper.Operator")))
			return true;
		return false;
	}

	public static ArrayList<IASTDeclaration> removeOperator(ArrayList<IASTDeclaration> variablesList) {
		ArrayList<IASTDeclaration> result = new ArrayList<IASTDeclaration>();

		for (IASTDeclaration m : variablesList) {
			if (m instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) m;
				if (!isOperator(simpleDeclaration))
					result.add(m);

			} else if (m instanceof IASTFunctionDefinition) {
				IASTFunctionDefinition funcdef = (IASTFunctionDefinition) m;
				if (!isOperator(funcdef))
					result.add(m);
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T extends IASTNode> T findParentOfType(Class<T> klass, IASTNode node) {
		while (node != null) {
			if (klass.isInstance(node)) {
				return (T) node; // here an unchecked warning is generated
									// because the code 'node instanceof T' is
									// not valid as condition in the
									// previous line.
			}
			node = node.getParent();
		}
		return null;
	}

	public static boolean isFunctor(IASTFunctionDeclarator functionDeclarator) {
		IASTName name = functionDeclarator.getName();
		if (name instanceof ICPPASTQualifiedName) {
			name = name.getLastName();
		}
		return name instanceof ICPPASTOperatorName;
	}

    public static ITranslationUnit getTranslationUnitFromIndexName(final IIndexName indexName) throws CoreException {
        final ICProject cProject = findProject(indexName);
        return CoreModelUtil.findTranslationUnitForLocation(indexName.getFile().getLocation(), cProject);
    }

    public static ICProject findProject(final IIndexName indexName) throws CoreException {
        final IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
        final IPath path = new Path(indexName.getFile().getLocation().toString());
        final IFile file = workspace.getFile(path);
        if (file == null || !file.exists()) {
            return null; // TODO: sensible message
        }
        final IProject project = file.getProject();
        if (project == null) {
            return null;
        }
        return CoreModel.getDefault().create(project);
    }
}
