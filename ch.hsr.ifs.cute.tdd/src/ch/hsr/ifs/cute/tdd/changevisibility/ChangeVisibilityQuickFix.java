/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.changevisibility;

import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.cute.tdd.CRefactoring3;
import ch.hsr.ifs.cute.tdd.TddQuickFix;

@SuppressWarnings("restriction")
public class ChangeVisibilityQuickFix extends TddQuickFix {

	public String getLabel() {
		return Messages.ChangeVisibilityQuickFix_0 + ca.getName() + Messages.ChangeVisibilityQuickFix_1;
	}

	@Override
	protected CRefactoring3 getRefactoring(RefactoringASTCache astCache, ITextSelection selection) {
		return new ChangeVisibilityRefactoring(selection, ca.getName(), astCache);
	}
}
