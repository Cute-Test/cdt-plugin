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

import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddErrorIdCollection;
import ch.hsr.ifs.cute.tdd.createfunction.CreateFreeFunctionRefactoring;
import ch.hsr.ifs.cute.tdd.createfunction.strategies.FunctionCreationStrategy;
import ch.hsr.ifs.cute.tdd.ui.tests.TddRefactoringTest;

public class CreateFunctionRefactoringTest extends TddRefactoringTest {

	public CreateFunctionRefactoringTest() {
		super(TddErrorIdCollection.ERR_ID_FunctionResolutionProblem, TddErrorIdCollection.ERR_ID_FunctionResolutionProblem_STATIC);
	}

	@Override
	protected CreateFreeFunctionRefactoring getRefactoring(IMarker marker, IDocument doc) throws CoreException {
		return new CreateFreeFunctionRefactoring(selection, new CodanArguments(marker), new FunctionCreationStrategy());
	}
}
