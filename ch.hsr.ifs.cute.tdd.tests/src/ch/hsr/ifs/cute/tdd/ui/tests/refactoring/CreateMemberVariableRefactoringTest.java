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

import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddErrorIdCollection;
import ch.hsr.ifs.cute.tdd.createvariable.CreateMemberVariableRefactoring;
import ch.hsr.ifs.cute.tdd.ui.tests.TddRefactoringTest;

@SuppressWarnings("restriction")
public class CreateMemberVariableRefactoringTest extends TddRefactoringTest {

	public CreateMemberVariableRefactoringTest() {
		super(TddErrorIdCollection.ERR_ID_MemberVariableResolutionProblem, TddErrorIdCollection.ERR_ID_VariableResolutionProblem);
	}

	@Override
	protected CRefactoring getRefactoring(IMarker marker, IDocument doc) throws CoreException {
		return new CreateMemberVariableRefactoring(selection, new CodanArguments(marker));
	}
}
