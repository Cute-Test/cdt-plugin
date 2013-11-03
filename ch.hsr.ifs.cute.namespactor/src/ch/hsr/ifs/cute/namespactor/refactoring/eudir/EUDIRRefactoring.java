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
package ch.hsr.ifs.cute.namespactor.refactoring.eudir;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.jface.viewers.ISelection;

import ch.hsr.ifs.cute.namespactor.astutil.ASTNodeFactory;
import ch.hsr.ifs.cute.namespactor.astutil.NSNameHelper;
import ch.hsr.ifs.cute.namespactor.refactoring.eu.EURefactoring;
import ch.hsr.ifs.cute.namespactor.refactoring.eu.EURefactoringContext;
import ch.hsr.ifs.cute.namespactor.refactoring.eu.EUReplaceVisitor;

/**
 * @author Jules Weder
 * */
@SuppressWarnings("restriction")
public class EUDIRRefactoring extends EURefactoring {

	public EUDIRRefactoring(ICElement element, ISelection selection, ICProject project) {
		super(element, selection, project);
	}

	@Override
	protected EUReplaceVisitor getReplaceVisitor() {
		return new EUDIRReplaceVisitor(context);
	}

	@Override
	protected IASTNode prepareInsertStatement() {
		ICPPASTUsingDirective newUsingDirective = ASTNodeFactory.getDefault().newUsingDirective(context.qualifiedUsingName);
		if (!(scopeNode instanceof ICPPASTNamespaceDefinition)) {
			return ASTNodeFactory.getDefault().newDeclarationStatement(newUsingDirective);
		}
		return newUsingDirective;
	}

	@Override
	protected ICPPASTQualifiedName buildUsingNameFrom(ICPPASTQualifiedName qualifiedName) {
		IASTName lastName = qualifiedName.getLastName();
		String[] names = NSNameHelper.getQualifiedUsingName(lastName.resolveBinding());

		if (names.length > 0) {
			ICPPASTQualifiedName qname = ASTNodeFactory.getDefault().newQualifiedNameNode(names);
			// Bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=381032
			qname.setFullyQualified(context.selectedQualifiedName.isFullyQualified());
			return qname;
		}
		return null;
	}

	@Override
	protected void findStartingNames(EURefactoringContext context) {
		IBinding binding;
		IASTName[] names = context.selectedQualifiedName.getNames();
		for (int i = names.length - 1; i >= 0; i--) {
			binding = names[i].resolveBinding();
			if (binding instanceof ICPPNamespace) {
				context.startingNamespaceName = names[i];
				return;
			}
		}
	}

	@Override
	protected IASTNode findTypeScope() {
		return null;
	}

}