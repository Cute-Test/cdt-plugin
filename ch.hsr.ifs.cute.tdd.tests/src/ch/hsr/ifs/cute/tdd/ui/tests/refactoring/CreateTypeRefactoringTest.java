/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.ui.tests.refactoring;

import java.util.ArrayList;

import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;

import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddErrorIdCollection;
import ch.hsr.ifs.cute.tdd.createtype.CreateTypeRefactoring;
import ch.hsr.ifs.cute.tdd.ui.tests.TddRefactoringTest;

@SuppressWarnings("restriction")
public class CreateTypeRefactoringTest extends TddRefactoringTest {

	public CreateTypeRefactoringTest(String name,
			ArrayList<com.includator.tests.base.TestSourceFile> files) {
		super(name, files, TddErrorIdCollection.ERR_ID_TypeResolutionProblem_HSR, TddErrorIdCollection.ERR_ID_NamespaceResolutionProblem_HSR);
	}

	protected CreateTypeRefactoring getRefactoring(IMarker marker, IDocument doc) {
		return new CreateTypeRefactoring(selection, new CodanArguments(marker), new RefactoringASTCache());
	}
}
