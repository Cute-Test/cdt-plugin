/*******************************************************************************
 * Copyright (c) 2010, 2011 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved.
 * 
 * Contributors:
 *     Institute for Software - initial API and implementation
 ******************************************************************************/
package com.includator.tests.base;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.TreeMap;
import java.util.Vector;

import org.eclipse.cdt.ui.tests.refactoring.TestHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public abstract class IncludatorTest extends IncludatorBaseTest {

	public static final String NL = System.getProperty("line.separator");

	public IncludatorTest(String name, Vector<TestSourceFile> files) {
		super(name, files);
	}

	
	public void openActiveFileInEditor() throws PartInitException, InterruptedException {
		final IFile file = project.getFile(activeFileName);
		openInEditor(file, true);
	}

	protected String makeProjectRelativePath(String absolutePath) {
		Path path = new Path(absolutePath);
		IPath location = project.getLocation();
		return path.makeRelativeTo(location).toOSString();
	}

	public static IEditorPart openInEditor(IFile file, boolean runEventLoop) throws PartInitException, InterruptedException {
		IEditorPart part = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		if (runEventLoop) {
			while (part.getSite().getShell().getDisplay().readAndDispatch()) {
				// do nothing
			}
		}
		return part;
	}
	protected void assertConditionsOk(RefactoringStatus conditions) {
		assertTrue(conditions.isOK() ? "OK" : "Error or Warning in Conditions: " + conditions.getEntries()[0].getMessage(), //$NON-NLS-1$ //$NON-NLS-2$
		conditions.isOK());
	}

	protected void assertConditionsWarning(RefactoringStatus conditions, int number) {
		if (number > 0) {
			assertTrue("Warning in Condition expected", conditions.hasWarning()); //$NON-NLS-1$
		}
		RefactoringStatusEntry[] entries = conditions.getEntries();
		int count = 0;
		for (RefactoringStatusEntry entry : entries) {
			if (entry.isWarning()) {
				++count;
			}
		}
		assertEquals(number + " Warnings expected found " + count, count, number); //$NON-NLS-1$
	}

	protected void assertConditionsInfo(RefactoringStatus status, int number) {
		if (number > 0) {
			assertTrue("Info in Condition expected", status.hasInfo()); //$NON-NLS-1$
		}
		RefactoringStatusEntry[] entries = status.getEntries();
		int count = 0;
		for (RefactoringStatusEntry entry : entries) {
			if (entry.isInfo()) {
				++count;
			}
		}
		assertEquals(number + " Infos expected found " + count, number, count); //$NON-NLS-1$
	}

	protected void assertConditionsError(RefactoringStatus status, int number) {
		if (number > 0) {
			assertTrue("Error in Condition expected", status.hasError()); //$NON-NLS-1$
		}
		RefactoringStatusEntry[] entries = status.getEntries();
		int count = 0;
		for (RefactoringStatusEntry entry : entries) {
			if (entry.isError()) {
				++count;
			}
		}
		assertEquals(number + " Errors expected found " + count, number, count); //$NON-NLS-1$
	}

	protected void assertConditionsFatalError(RefactoringStatus status, int number) {
		if (number > 0) {
			assertTrue("Fatal Error in Condition expected", status.hasFatalError()); //$NON-NLS-1$
		}
		RefactoringStatusEntry[] entries = status.getEntries();
		int count = 0;
		for (RefactoringStatusEntry entry : entries) {
			if (entry.isFatalError()) {
				++count;
			}
		}
		assertEquals(number + " Fatal Errors expected found " + count, number, count); //$NON-NLS-1$
	}

	protected void assertConditionsFatalError(RefactoringStatus conditions) {
		assertTrue("Fatal Error in Condition expected", conditions.hasFatalError()); //$NON-NLS-1$
	}
	
	protected void compareFiles(TreeMap<String,TestSourceFile> testResourceFiles) throws Exception {
		for (String fileName : testResourceFiles.keySet()) {
			String expectedSource = testResourceFiles.get(fileName).getExpectedSource();
			IFile iFile = project.getFile(new Path(fileName));
			String code = getCodeFromIFile(iFile);
			assertEquals(TestHelper.unifyNewLines(expectedSource), TestHelper.unifyNewLines(code));
		}
	}
	
	protected String getCodeFromIFile(IFile file) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(file.getContents()));
		StringBuilder code = new StringBuilder();
		String line;
		while((line = br.readLine()) != null) {
			code.append(line);
			code.append(NL);
		}
		br.close();
		return code.toString();
	}
}
