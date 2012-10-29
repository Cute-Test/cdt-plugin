/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.ui.tests.refactoring;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;

import ch.hsr.ifs.cute.tdd.TddErrorIdCollection;
import ch.hsr.ifs.cute.tdd.addArgument.AddArgumentRefactoring;
import ch.hsr.ifs.cute.tdd.ui.tests.TddRefactoringTest;

public class AddArgumentRefactoringTest extends TddRefactoringTest {

	public AddArgumentRefactoringTest() {
		super(TddErrorIdCollection.ERR_ID_InvalidArguments_HSR, TddErrorIdCollection.ERR_ID_InvalidArguments_FREE_HSR);
	}

	@Override
	protected AddArgumentRefactoring getRefactoring(IMarker marker, IDocument doc) throws CoreException {
		return new AddArgumentRefactoring(selection, candidate);
	}
}