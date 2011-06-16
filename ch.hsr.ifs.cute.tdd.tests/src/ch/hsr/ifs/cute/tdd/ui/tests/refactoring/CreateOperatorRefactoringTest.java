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

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNameBase;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddErrorIdCollection;
import ch.hsr.ifs.cute.tdd.createfunction.CreateMemberFunctionRefactoring;
import ch.hsr.ifs.cute.tdd.createfunction.strategies.OperatorCreationStrategy;
import ch.hsr.ifs.cute.tdd.ui.tests.TddRefactoringTest;

import com.includator.tests.base.TestSourceFile;

@SuppressWarnings("restriction")
public class CreateOperatorRefactoringTest extends TddRefactoringTest {

	public CreateOperatorRefactoringTest(String name,
			ArrayList<TestSourceFile> files) {
		super(name, files, TddErrorIdCollection.ERR_ID_OperatorResolutionProblem_HSR);
		CPPASTNameBase.sAllowNameComputation = true;
	}

	@Override
	protected Refactoring getRefactoring(IMarker marker, IDocument doc)
			throws CoreException {
		return new CreateMemberFunctionRefactoring(selection, new CodanArguments(marker), new RefactoringASTCache(), new OperatorCreationStrategy(false));
	}

}
