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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.cute.tdd.TddErrorIdCollection;
import ch.hsr.ifs.cute.tdd.TddHelper;
import ch.hsr.ifs.cute.tdd.changevisibility.ChangeVisibilityRefactoring;
import ch.hsr.ifs.cute.tdd.ui.tests.TddRefactoringTest;

import com.includator.tests.base.TestSourceFile;

@SuppressWarnings("restriction")
public class ChangeVisibilityRefactoringTest extends TddRefactoringTest {

	public ChangeVisibilityRefactoringTest(String name,
			ArrayList<TestSourceFile> files) {
		super(name, files, TddErrorIdCollection.ERR_ID_PrivateMethodChecker_HSR);
	}

	protected Refactoring getRefactoring(IMarker marker, IDocument doc) throws CoreException {
		String missingName = TddHelper.extractMissingFunctionName(marker, doc);
		return new ChangeVisibilityRefactoring(selection, missingName, new RefactoringASTCache());
	}
}