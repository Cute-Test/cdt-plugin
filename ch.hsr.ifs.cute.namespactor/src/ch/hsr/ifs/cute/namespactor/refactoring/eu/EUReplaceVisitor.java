/******************************************************************************
 * Copyright (c) 2012 Institute for Software, HSR Hochschule fuer Technik 
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 *
 * Contributors:
 * 	Ueli Kunz <kunz@ideadapt.net>, Jules Weder <julesweder@gmail.com> - initial API and implementation
 ******************************************************************************/
package ch.hsr.ifs.cute.namespactor.refactoring.eu;

import org.eclipse.cdt.codan.core.cxx.CxxAstUtils;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;

import ch.hsr.ifs.cute.namespactor.astutil.ASTNodeFactory;
import ch.hsr.ifs.cute.namespactor.astutil.NSNodeHelper;
import ch.hsr.ifs.cute.namespactor.astutil.NSSelectionHelper;

/**
 * @author Jules Weder
 * */
@SuppressWarnings("restriction")
public abstract class EUReplaceVisitor extends ASTVisitor {

	protected EURefactoringContext context;

	{
		shouldVisitNames = true;
	}

	@Override
	public int visit(IASTName name) {
		if (name instanceof ICPPASTQualifiedName) {
			IASTName replacementName = buildReplacementName(name);
			if (replacementName != null) {
				if (((ICPPASTQualifiedName) replacementName).getNames().length <= 1) {
					replacementName = replacementName.getLastName();
				}
				if (replacementName == null && name.getParent() instanceof ICPPASTUsingDirective) {
					removeUselessUsingDirective(name);
					return PROCESS_SKIP;
				}
				replace(name, replacementName);
			}
		}
		removeUnqualifiedUsingDirective(name);
		return PROCESS_SKIP;

	}

	private void replace(IASTName name, IASTName replacementName) {
		context.nodesToReplace.put(name, replacementName);
		prepareInsertionPoint(name);
	}

	protected void removeUselessUsingDirective(IASTName name) {
		IASTNode parent = name.getParent();
		context.nodesToRemove.add(parent);
		prepareInsertionPoint(parent);
	}

	protected void prepareInsertionPoint(IASTNode node) {
		if (context.firstNameToReplace == null) {
			context.firstNameToReplace = node;
		}
	}

	protected IASTName buildReplacementName(IASTName name) { // TODO: debug and fix with new template mechanism
		if (isExtractCandidate(name)) {
			ICPPASTQualifiedName replaceName = ASTNodeFactory.getDefault().newQualifiedName();
			IASTName[] names = getNamesOf(name);
			IASTName foundName = searchNamesFor(context.startingNamespaceName, names);
			if (foundName != null && CxxAstUtils.isInMacro(foundName))
				return null;
			if (isReplaceCandidate(foundName, name, names)) {
				boolean start = false;
				for (IASTName iastName : names) {
					if (isTemplateReplaceCandidate(foundName, iastName)) {
						replaceName = buildReplacementTemplate(iastName);
						continue;
					}
					if (start) {
						replaceName.addName(iastName.copy());
					}
					if (isNameFound(foundName, iastName)) {
						start = true;
					}
				}
				if (isUnqualifiedDefinition(name, replaceName)) {
					return null;
				}
				return replaceName;
			} else {
				// Bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=381032 // PS: fixed by Thomas in 2013, still required?
				// if (context.selectedQualifiedName.isFullyQualified()) {
				// buildFullyQualifiedReplaceName(replaceName, names);
				// return replaceName;
				// }
			}
		}
		return null;
	}

	protected void removeUnqualifiedUsingDirective(IASTName name) {

	}

	protected static IASTName[] getNamesOf(IASTName name) {
		IASTName[] names = null;
		if (name instanceof ICPPASTQualifiedName) {
			names = ((ICPPASTQualifiedName) name).getNames();
		} else {
			names = new IASTName[] { name.getLastName() };
		}
		return names;
	}

	protected static boolean isExtractCandidate(IASTName name) {
		return NSSelectionHelper.isSelectionCandidate(name) || NSNodeHelper.hasAncestor(name, ICPPASTUsingDirective.class);
	}

	protected abstract IASTName searchNamesFor(IASTName name, IASTName[] names);

	protected abstract boolean isReplaceCandidate(IASTName foundName, IASTName name, IASTName[] names);

	protected boolean isTemplateReplaceCandidate(IASTName foundName, IASTName iastName) {
		return iastName instanceof ICPPASTTemplateId && !(foundName instanceof ICPPASTTemplateId);
	}

	protected abstract ICPPASTQualifiedName buildReplacementTemplate(IASTName iastName);

	protected abstract boolean isNameFound(IASTName foundName, IASTName iastName);

	protected boolean isUnqualifiedDefinition(IASTName name, ICPPASTQualifiedName replaceName) {
		if (isFunctionDefinition(name) || name.getParent() instanceof ICPPASTCompositeTypeSpecifier) {
			if (!(replaceName.getNames().length > 1)) {
				return true;
			}
		}
		return false;
	}

	protected static boolean isFunctionDefinition(IASTName name) {
		return name.getParent().getParent() instanceof ICPPASTFunctionDefinition;
	}

	protected void buildFullyQualifiedReplaceName(ICPPASTQualifiedName replaceName, IASTName[] names) {
	}

}
