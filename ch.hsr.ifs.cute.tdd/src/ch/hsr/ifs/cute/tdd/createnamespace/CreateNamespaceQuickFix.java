/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.createnamespace;

import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.cute.tdd.CRefactoring3;
import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddQuickFix;

@SuppressWarnings("restriction")
public class CreateNamespaceQuickFix extends TddQuickFix {

	@Override
	public String getLabel() {
		CodanArguments ca = new CodanArguments(marker);
		return "Create namespace " + ca.getName();
	}

	@Override
	protected CRefactoring3 getRefactoring(RefactoringASTCache astCache, ITextSelection selection) {
		return new CreateNamespaceRefactoring(selection, ca.getName(), astCache);
	}
}
