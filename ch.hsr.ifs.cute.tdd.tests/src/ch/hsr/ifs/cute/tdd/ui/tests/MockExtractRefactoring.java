/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.ui.tests;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.jface.viewers.ISelection;

import ch.hsr.ifs.cute.tdd.extract.ExtractRefactoring;

@SuppressWarnings("restriction")
public class MockExtractRefactoring extends ExtractRefactoring {

	private boolean shouldoverwrite;

	public MockExtractRefactoring(ICElement icElement, ISelection selection,
			ICProject proj, RefactoringASTCache astCache) {
		super(icElement, selection, astCache);
	}
	
	public void setOverwriteAnswer(boolean overwrite) {
		this.shouldoverwrite = overwrite;
	}

	@Override
	protected boolean shouldOverwriteOnUserRequest(String name) {
		return shouldoverwrite;
	}	
}
