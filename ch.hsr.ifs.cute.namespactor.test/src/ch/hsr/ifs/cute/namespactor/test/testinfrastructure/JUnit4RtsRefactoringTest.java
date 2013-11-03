/******************************************************************************
 * Copyright (c) 2012 Institute for Software, HSR Hochschule fuer Technik 
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 *
 * Contributors:
 * 	Ueli Kunz <kunz@ideadapt.net>, Jules Weder <julesweder@gmail.com> - initial API and implementation
 ******************************************************************************/
package ch.hsr.ifs.cute.namespactor.test.testinfrastructure;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.junit.After;
import org.junit.Test;

import ch.hsr.ifs.cdttesting.rts.junit4.CDTProjectJUnit4RtsTest;
import ch.hsr.ifs.cute.namespactor.test.TestActivator;

@SuppressWarnings("restriction")
public abstract class JUnit4RtsRefactoringTest extends CDTProjectJUnit4RtsTest implements ILogListener {

	protected int expectedNrOfWarnings = 0;
	protected int expectedNrOfErrors = 0;
	protected int expectedNrOfFatalErrors = 0;
	protected int skipTest = 0;
	protected CRefactoring refactoring;

	IStatus loggedStatus;
	String loggingPlugin;
	private CRefactoringContext cRefactoringContext;

	@Override
	@After
	public void tearDown() throws Exception {
		if (cRefactoringContext != null) {
			cRefactoringContext.dispose();
			cRefactoringContext = null;
		}
		super.tearDown();
	}

	@Override
	@Test
	public void runTest() throws Throwable {

		if (skipTest != 0) {
			TestActivator.log(String.format("Test configured to be skipped. Skipping test: %s%n", getName()));
			return;
		}

		TestActivator.log(String.format("-- Before Refactoring - TestSourceFile: %s%n", getName()));
		TestActivator.log(fileMap.get(activeFileName).getSource());
		TestActivator.log(String.format("--%n"));

		refactoring = getRefactoring();
		cRefactoringContext = new CRefactoringContext(refactoring);

		if (!checkInitialConditions()) {
			compareInitialWidthExpectedSource();
			return;
		}

		checkFinalConditions();

		Change change = refactoring.createChange(NULL_PROGRESS_MONITOR);

		change.perform(NULL_PROGRESS_MONITOR);

		compareInitialWidthExpectedSource();
	}

	protected String getCodeFromIFile(IFile file) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(file.getContents()));
		StringBuilder code = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			code.append(line);
			code.append(System.getProperty("line.separator"));
		}
		br.close();
		return code.toString();
	}

	protected ICElement getCElementOfTestFile() throws CModelException {
		return cproject.findElement(project.getFile(activeFileName).getFullPath());
	}

	protected void assertConditionsOk(RefactoringStatus conditions) {
		assertTrue(conditions.isOK() ? "OK" : "Error or Warning in Conditions: " + conditions.getEntries()[0].getMessage(), conditions.isOK());
	}

	protected void assertConditionsWarning(RefactoringStatus conditions, int number) {
		if (number > 0) {
			assertTrue("Warning in Condition expected", conditions.hasWarning());
		}
		RefactoringStatusEntry[] entries = conditions.getEntries();
		int count = 0;
		for (RefactoringStatusEntry entry : entries) {
			if (entry.isWarning()) {
				++count;
			}
		}
		assertEquals(number + " Warnings expected found " + count, count, number);
	}

	protected void assertConditionsInfo(RefactoringStatus status, int number) {
		if (number > 0) {
			assertTrue("Info in Condition expected", status.hasInfo());
		}
		RefactoringStatusEntry[] entries = status.getEntries();
		int count = 0;
		for (RefactoringStatusEntry entry : entries) {
			if (entry.isInfo()) {
				++count;
			}
		}
		assertEquals(number + " Infos expected found " + count, number, count);
	}

	protected void assertConditionsError(RefactoringStatus status, int number) {
		if (number > 0) {
			assertTrue("Error in Condition expected", status.hasError());
		}
		RefactoringStatusEntry[] entries = status.getEntries();
		int count = 0;
		for (RefactoringStatusEntry entry : entries) {
			if (entry.isError()) {
				++count;
			}
		}
		assertEquals(number + " Errors expected found " + count, number, count);
	}

	protected void assertConditionsFatalError(RefactoringStatus status, int number) {
		if (number > 0) {
			assertTrue("Fatal Error in Condition expected", status.hasFatalError());
		}
		RefactoringStatusEntry[] entries = status.getEntries();
		int count = 0;
		for (RefactoringStatusEntry entry : entries) {
			if (entry.isFatalError()) {
				++count;
			}
		}
		assertEquals(number + " Fatal Errors expected found " + count, number, count);
	}

	protected boolean checkInitialConditions() throws CoreException {
		RefactoringStatus initialConditions = refactoring.checkInitialConditions(NULL_PROGRESS_MONITOR);

		if (expectedNrOfErrors > 0) {
			assertConditionsError(initialConditions, expectedNrOfErrors);
		} else if (expectedNrOfWarnings > 0) {
			assertConditionsWarning(initialConditions, expectedNrOfWarnings);
		} else if (expectedNrOfFatalErrors > 0) {
			assertConditionsFatalError(initialConditions, expectedNrOfFatalErrors);
		} else {
			assertConditionsOk(initialConditions);
			return true;
		}
		return false;
	}

	protected void checkFinalConditions() throws CoreException {
		RefactoringStatus finalConditions = refactoring.checkFinalConditions(NULL_PROGRESS_MONITOR);

		if (expectedNrOfWarnings > 0) {
			assertConditionsWarning(finalConditions, expectedNrOfWarnings);
		} else {
			assertConditionsOk(finalConditions);
		}
	}

	protected void compareInitialWidthExpectedSource() throws Exception {

		for (String fileName : fileMap.keySet()) {
			String expectedSource = fileMap.get(fileName).getExpectedSource();
			IFile file = project.getFile(new Path(fileName));
			String refactoredCode = getCodeFromIFile(file);
			assertEquals(expectedSource, refactoredCode);
		}
	}

	@Override
	protected void configureTest(Properties properties) {
		skipTest = new Integer(properties.getProperty("skipTest", "0")).intValue();
		expectedNrOfWarnings = new Integer(properties.getProperty("expectedNrOfWarnings", "0")).intValue();
		expectedNrOfErrors = new Integer(properties.getProperty("expectedNrOfErrors", "0")).intValue();
		expectedNrOfFatalErrors = new Integer(properties.getProperty("expectedNrOfFatalErrors", "0")).intValue();
	}

	@Override
	public void logging(IStatus status, String plugin) {
		loggedStatus = status;
		loggingPlugin = plugin;
	}

	protected abstract CRefactoring getRefactoring() throws CModelException;
}