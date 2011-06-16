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
import org.eclipse.jface.text.TextSelection;

import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddErrorIdCollection;
import ch.hsr.ifs.cute.tdd.createfunction.CreateFreeFunctionRefactoring;
import ch.hsr.ifs.cute.tdd.createfunction.strategies.OperatorCreationStrategy;
import ch.hsr.ifs.cute.tdd.ui.tests.TddRefactoringTest;

@SuppressWarnings("restriction")
public class CreateFreeOperatorRefactoringTest extends TddRefactoringTest {

	public CreateFreeOperatorRefactoringTest(String name,
			ArrayList<com.includator.tests.base.TestSourceFile> files) {
		super(name, files, TddErrorIdCollection.ERR_ID_OperatorResolutionProblem_HSR);
	}

	protected CreateFreeFunctionRefactoring getRefactoring(IMarker marker, IDocument doc) throws CoreException {
		CodanArguments ca = new CodanArguments(marker);
		int markerOffset = marker.getAttribute(IMarker.CHAR_START, 0);
		ca.setStrategy(":freeoperator");
		return new CreateFreeFunctionRefactoring(new TextSelection(doc, markerOffset, marker.getAttribute(IMarker.CHAR_END, 0)-markerOffset), ca, new RefactoringASTCache(), new OperatorCreationStrategy(true));
	}

}
