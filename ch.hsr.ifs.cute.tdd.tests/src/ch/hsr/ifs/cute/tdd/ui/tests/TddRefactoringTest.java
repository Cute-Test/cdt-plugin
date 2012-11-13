/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.ui.tests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemProfile;
import org.eclipse.cdt.codan.core.model.IProblemReporter;
import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.param.RootProblemPreference;
import org.eclipse.cdt.codan.internal.core.CodanBuilder;
import org.eclipse.cdt.codan.internal.core.model.CodanProblem;
import org.eclipse.cdt.core.parser.tests.rewrite.TestHelper;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.Test;

import ch.hsr.ifs.cdttesting.JUnit4RtsTest;
import ch.hsr.ifs.cdttesting.TestSourceFile;
import ch.hsr.ifs.cute.tdd.CRefactoring3;

@SuppressWarnings("restriction")
public abstract class TddRefactoringTest extends JUnit4RtsTest {

	private static final int EMPTY_SELECTION = 0;
	public static final String NL = System.getProperty("line.separator");
	private final String[] problems;
	private String[] newFiles;

	public static boolean NO_MARKER_DEFAULT = false;
	public static int MARKER_COUNT_DEFAULT = 1;
	public static boolean TYPE_EXTRACTION_DEFAULT = false;
	public static boolean IGNORE_COMMENTS_DEFAULT = false;
	public static boolean OVERWRITE_DEFAULT = true;
	public static int CANDIDATE_DEFAULT = 0;

	protected boolean noMarker;
	protected int markerCount;
	protected boolean typeExtraction;
	protected boolean ignoreComments;
	protected boolean overwrite;
	protected int candidate;
	protected TextSelection selection;

	public TddRefactoringTest(String... problem) {
		this.problems = problem;
	}

	@Override
	@Test
	public void runTest() throws Throwable {
		CRefactoring refactoring = null;
		CRefactoringContext context = null;
		try {
			enablePassedProblems();
			if (typeExtraction) {
				setSelectionOnFile();
				refactoring = getRefactoring(null, null);
				context = new CRefactoringContext(refactoring);
				createAndPerformChange(refactoring);
				for (String filename : fileMap.keySet()) {
					IFile iFile = project.getFile(new Path(filename));
					iFile = getFile(iFile, filename);
					String code = getCodeFromIFile(iFile);
					String expectedSource = fileMap.get(filename).getExpectedSource();
					if (ignoreComments) {
						code = removeCommentsFromCode(code);
					}
					assertEquals(TestHelper.unifyNewLines(expectedSource), TestHelper.unifyNewLines(code));
				}
				return;
			}
			IMarker[] markers = getCodanMarker();
			assertEquals("Unexpected marker count.", markerCount, markers.length);

			if (markers.length > 0) {
				IMarker marker = markers[0];
				IDocument doc = openDocument(marker);
				setSelection(new TextSelection(doc, getOffset(marker, doc), EMPTY_SELECTION));
				refactoring = getRefactoring(marker, doc);
				context = new CRefactoringContext(refactoring);
				refactoring.setContext(context);
				createAndPerformChange(refactoring);
			}
			compareFiles(fileMap);
		} finally {
			if (refactoring instanceof CRefactoring3) {
				((CRefactoring3) refactoring).dispose();
			}
		}
	}

	private void enablePassedProblems() {
		if (problems.length == 0) {
			enableAllProblems();
		} else {
			enableProblems(problems);
		}
	}

	private IFile getFile(IFile file, String filename) {
		if (!overwrite) {
			return file;
		}
		String normalfilename = normalizeFileName(filename);
		for (String newfile : newFiles) {
			if (normalfilename.equals(newfile)) {
				file = project.getFile(new Path(newfile));
				break;
			}
		}
		return file;
	}

	private String normalizeFileName(String filename) {
		String normalfilename = filename.replaceAll("_", "");
		normalfilename = normalfilename.replaceAll("\\d", "");
		return normalfilename;
	}

	private void setSelectionOnFile() {
		for (Entry<String, TestSourceFile> entry : fileMap.entrySet()) {
			TestSourceFile file = entry.getValue();
			//Normally we have only one selection
			if (file.getSelection() == null)
				continue;
			setSelection(file.getSelection());
			break;
		}
	}

	private void createAndPerformChange(Refactoring refactoring) throws CoreException {
		assertConditionsOk(refactoring.checkInitialConditions(NULL_PROGRESS_MONITOR));
		assertConditionsOk(refactoring.checkFinalConditions(NULL_PROGRESS_MONITOR));
		Change changes = refactoring.createChange(NULL_PROGRESS_MONITOR);
		changes.perform(NULL_PROGRESS_MONITOR);
	}

	private String removeCommentsFromCode(String code) {
		String separator = System.getProperty("line.separator");
		String[] lines = code.split(separator);
		code = "";
		for (String line : lines) {
			Matcher m = Pattern.compile("[/\\*|\\*].*").matcher(line);
			if (!m.find() || line.startsWith("#")) {
				code += line + separator;
			}
		}
		return code;
	}

	protected void enableProblems(String... ids) {
		IProblemProfile profile = CodanRuntime.getInstance().getCheckersRegistry().getWorkspaceProfile();
		IProblem[] problems = profile.getProblems();
		for (int i = 0; i < problems.length; i++) {
			IProblem p = problems[i];
			boolean enabled = false;
			for (int j = 0; j < ids.length; j++) {
				String pid = ids[j];
				if (p.getId().equals(pid)) {
					enabled = true;
					// Force the launch mode to FULL_BUILD to make sure we can test the problem even if by default it
					// is not set to run on FULL_BUILD
					IProblemPreference preference = p.getPreference();
					if (preference instanceof RootProblemPreference) {
						RootProblemPreference rootProblemPreference = (RootProblemPreference) preference;
						rootProblemPreference.getLaunchModePreference().enableInLaunchModes(CheckerLaunchMode.RUN_ON_FULL_BUILD);
					}
					break;
				}
			}
			((CodanProblem) p).setEnabled(enabled);
		}
		CodanRuntime.getInstance().getCheckersRegistry().updateProfile(cproject.getProject(), profile);
		return;
	}

	protected void enableAllProblems() {
		IProblemProfile profile = CodanRuntime.getInstance().getCheckersRegistry().getWorkspaceProfile();
		IProblem[] problems = profile.getProblems();
		for (int i = 0; i < problems.length; i++) {
			IProblem p = problems[i];
			((CodanProblem) p).setEnabled(true);
		}
		CodanRuntime.getInstance().getCheckersRegistry().updateProfile(cproject.getProject(), profile);
		return;
	}

	protected abstract CRefactoring getRefactoring(IMarker marker, IDocument doc) throws CoreException;

	private IMarker[] getCodanMarker() {
		IMarker[] markers = new IMarker[] {};
		try {
			CodanBuilder builder = (CodanBuilder) CodanRuntime.getInstance().getBuilder();
			builder.processResource(cproject.getProject().getFile(activeFileName), NULL_PROGRESS_MONITOR);
			markers = cproject.getProject().findMarkers(IProblemReporter.GENERIC_CODE_ANALYSIS_MARKER_TYPE, true, 1);
			builder.forgetLastBuiltState();
		} catch (CoreException e) {
			fail(e.getMessage());
		}
		return markers;
	}

	private int getOffset(IMarker marker, IDocument doc) {
		int charStart = marker.getAttribute(IMarker.CHAR_START, -1);
		if (charStart > 0)
			return charStart;
		try {
			return doc.getLineOffset(marker.getAttribute(IMarker.LINE_NUMBER, -1) - 1);
		} catch (BadLocationException e) {
			return -1;
		}
	}

	private IDocument openDocument(IMarker marker) {
		return openDocument(openEditor(marker));
	}

	private IEditorPart openEditor(IMarker marker) {
		try {
			return IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), (IFile) marker.getResource());
		} catch (PartInitException e) {
			e.printStackTrace();
			return null;
		}
	}

	private IDocument openDocument(IEditorPart editorPart) {
		if (editorPart instanceof ITextEditor) {
			ITextEditor editor = (ITextEditor) editorPart;
			IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
			return doc;
		}
		return null;
	}

	@Override
	protected void configureTest(Properties properties) {
		noMarker = Boolean.valueOf(properties.getProperty("nomarkers", Boolean.toString(NO_MARKER_DEFAULT)));
		markerCount = Integer.valueOf(properties.getProperty("markerCount", Integer.toString(MARKER_COUNT_DEFAULT)));
		typeExtraction = Boolean.valueOf(properties.getProperty("typeextraction", Boolean.toString(TYPE_EXTRACTION_DEFAULT)));
		//TODO: do not overwrite files not yet tested
		overwrite = Boolean.valueOf(properties.getProperty("overwrite", Boolean.toString(OVERWRITE_DEFAULT)));
		newFiles = separateNewFiles(properties);
		ignoreComments = Boolean.valueOf(properties.getProperty("ignorecomments", Boolean.toString(IGNORE_COMMENTS_DEFAULT)));
		candidate = Integer.valueOf(properties.getProperty("candidate", Integer.toString(CANDIDATE_DEFAULT)));
	};

	private String[] separateNewFiles(Properties refactoringProperties) {
		return String.valueOf(refactoringProperties.getProperty("newfiles", "")).replace(" ", "").split(",");
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

	private void setSelection(TextSelection selection) {
		this.selection = selection;
	}

	protected void compareFiles(TreeMap<String, TestSourceFile> testResourceFiles) throws Exception {
		for (String fileName : testResourceFiles.keySet()) {
			String expectedSource = testResourceFiles.get(fileName).getExpectedSource();
			IFile iFile = project.getFile(new Path(fileName));
			String code = getCodeFromIFile(iFile);
			assertEquals(TestHelper.unifyNewLines(expectedSource), TestHelper.unifyNewLines(code));
		}
	}

	protected void assertConditionsOk(RefactoringStatus conditions) {
		assertTrue(
				conditions.isOK() ? "OK" : "Error or Warning in Conditions: " + conditions.getEntries()[0].getMessage(), //$NON-NLS-1$ //$NON-NLS-2$
				conditions.isOK());
	}
}
