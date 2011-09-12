/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.createnamespace;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamespaceDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.cdt.internal.ui.refactoring.togglefunction.ToggleNodeHelper;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.cute.tdd.CRefactoring3;
import ch.hsr.ifs.cute.tdd.TddHelper;
import ch.hsr.ifs.cute.tdd.createtype.CreateTypeRefactoring;

public class CreateNamespaceRefactoring extends CRefactoring3 {

	public CreateNamespaceRefactoring(ITextSelection textSelection,
			String name, RefactoringASTCache astCache) {
		super(textSelection, astCache);
	}

	@Override
	protected void collectModifications(IProgressMonitor pm,
			ModificationCollector collector) throws CoreException,
			OperationCanceledException {
		IASTTranslationUnit localunit = astCache.getAST(tu, pm);
		IASTNode selectedNode = localunit.getNodeSelector(null).findEnclosingName(getSelection().getOffset(), getSelection().getLength());
		IASTName selectedNodeName = ToggleNodeHelper.getAncestorOfType(selectedNode, CPPASTName.class);
		ICPPASTQualifiedName qname = ToggleNodeHelper.getAncestorOfType(selectedNode, CPPASTQualifiedName.class);
		if (selectedNode instanceof IASTName && qname != null) {
			ICPPASTNamespaceDefinition ns = new CPPASTNamespaceDefinition(selectedNodeName.copy());
			IASTNode insertionPoint = CreateTypeRefactoring.getInsertionPoint(localunit, selectedNodeName, astCache);
		
			//TODO: Remvoe duplicated crom createclass
			if (insertionPoint instanceof CPPASTCompositeTypeSpecifier || insertionPoint instanceof CPPASTNamespaceDefinition) {
				TddHelper.writeDefinitionTo(collector, insertionPoint, ns);
			} else {
				ASTRewrite rewrite = collector.rewriterForTranslationUnit(localunit);
				rewrite.insertBefore(insertionPoint.getParent(), insertionPoint, ns, null);
			}
		}
	}
	
}
