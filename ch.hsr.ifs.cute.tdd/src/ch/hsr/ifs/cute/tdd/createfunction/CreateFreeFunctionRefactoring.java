/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.createfunction;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.viewers.ISelection;

import ch.hsr.ifs.cute.tdd.CRefactoring3;
import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddHelper;
import ch.hsr.ifs.cute.tdd.TypeHelper;
import ch.hsr.ifs.cute.tdd.createfunction.strategies.IFunctionCreationStrategy;

@SuppressWarnings("restriction")
public class CreateFreeFunctionRefactoring extends CRefactoring3 {
	
	private CodanArguments ca;
	private IFunctionCreationStrategy strategy;

	public CreateFreeFunctionRefactoring(ISelection selection, CodanArguments ca, RefactoringASTCache astCache, IFunctionCreationStrategy functionCreationStrategy) {
		super(selection, astCache);
		this.ca = ca;
		this.strategy = functionCreationStrategy;
	}

	protected void collectModifications(IProgressMonitor pm,
			ModificationCollector collector) throws CoreException,
			OperationCanceledException {
		ICPPASTFunctionDefinition functionToWrite = null;

		IASTTranslationUnit localunit = astCache.getAST(tu, pm);
		IASTName selectedName = FunctionCreationHelper.getMostCloseSelectedNodeName(localunit, getSelection());
		ICPPASTCompositeTypeSpecifier owningType = TypeHelper.getTargetTypeOfField(localunit, selectedName, astCache);
		functionToWrite = strategy.getFunctionDefinition(localunit, selectedName, owningType, ca.getName(), getSelection());
		
		if (ca.isStaticCase()) {
			IASTNode parent = TddHelper.getNestedInsertionPoint(localunit, selectedName.getParent(), astCache);
			TddHelper.writeDefinitionTo(collector, parent, functionToWrite);
			return;
		}
		
		((ICPPASTFunctionDeclarator) functionToWrite.getDeclarator()).setConst(false);
		IASTFunctionDefinition outerFunction = TddHelper.getOuterFunctionDeclaration(localunit, getSelection());

		ASTRewrite rewrite = collector.rewriterForTranslationUnit(localunit);
		rewrite.insertBefore(outerFunction.getParent(), outerFunction, functionToWrite, null);

		setLinkedModeInformation(localunit, owningType, functionToWrite);
	}
}
