/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.createfunction;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
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
public class CreateMemberFunctionRefactoring extends CRefactoring3 {

	public CodanArguments ca;
	private IFunctionCreationStrategy strategy;

	public CreateMemberFunctionRefactoring(ISelection selection, CodanArguments ca, RefactoringASTCache astCache, IFunctionCreationStrategy strategy) {
		super(selection, astCache);
		this.ca = ca;
		this.strategy = strategy;
	}

	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector) 
			throws CoreException, OperationCanceledException {
		IASTTranslationUnit localunit = astCache.getAST(tu, pm);
		IASTName selectedNode = FunctionCreationHelper.getMostCloseSelectedNodeName(localunit, getSelection());
		ICPPASTCompositeTypeSpecifier type = TypeHelper.getTargetTypeOfField(localunit, selectedNode, astCache);
		ICPPASTFunctionDefinition newFunction = strategy.getFunctionDefinition(localunit, selectedNode, type, ca.getName(), getSelection());
		TddHelper.writeDefinitionTo(collector, type, newFunction);
		setLinkedModeInformation(localunit, type, newFunction);
		setAditionalLinkedModeInformation();
	}

	private void setAditionalLinkedModeInformation() {
		if (!ca.isCtorCase()) {
			lmi.sethasDeclSpec(true);
		}
	}
}