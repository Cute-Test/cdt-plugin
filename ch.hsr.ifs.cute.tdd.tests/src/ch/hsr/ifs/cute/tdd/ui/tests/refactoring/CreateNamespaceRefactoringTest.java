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

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;

import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddErrorIdCollection;
import ch.hsr.ifs.cute.tdd.createnamespace.CreateNamespaceRefactoring;
import ch.hsr.ifs.cute.tdd.ui.tests.TddRefactoringTest;

import com.includator.tests.base.TestSourceFile;

public class CreateNamespaceRefactoringTest extends TddRefactoringTest {

	public CreateNamespaceRefactoringTest(String name, ArrayList<TestSourceFile> files) {
		super(name, files, TddErrorIdCollection.ERR_ID_NamespaceResolutionProblem_HSR);
	}

	@Override
	protected CRefactoring getRefactoring(IMarker marker, IDocument doc) throws CoreException {
		return new CreateNamespaceRefactoring(selection, new CodanArguments(marker).getName());
	}
}
