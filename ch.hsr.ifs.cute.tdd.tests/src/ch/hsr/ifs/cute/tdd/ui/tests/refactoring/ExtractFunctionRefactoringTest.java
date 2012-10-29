/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.ui.tests.refactoring;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;

import ch.hsr.ifs.cute.tdd.ui.tests.MockExtractRefactoring;
import ch.hsr.ifs.cute.tdd.ui.tests.TddRefactoringTest;

@SuppressWarnings("restriction")
public class ExtractFunctionRefactoringTest extends TddRefactoringTest {

	@Override
	protected CRefactoring getRefactoring(IMarker marker, IDocument doc) throws CoreException {
		MockExtractRefactoring refactoring = new MockExtractRefactoring(cproject.findElement(project.getFile(activeFileName).getFullPath()), selection, cproject);
		refactoring.setOverwriteAnswer(overwrite);
		return refactoring;
	}
}
