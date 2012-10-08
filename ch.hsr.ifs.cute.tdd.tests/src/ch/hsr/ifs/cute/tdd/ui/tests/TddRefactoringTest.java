/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.ui.tests;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Properties;
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
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.tests.rewrite.TestHelper;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.Test;

import ch.hsr.ifs.cute.tdd.CRefactoring3;

import com.includator.tests.base.JUnit4IncludatorTest;
import com.includator.tests.base.TestSourceFile;

@SuppressWarnings("restriction")
public abstract class TddRefactoringTest extends JUnit4IncludatorTest {

	private static final int EMPTY_SELECTION = 0;
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

	public TddRefactoringTest(String name, ArrayList<com.includator.tests.base.TestSourceFile> files, String... problem) {
		super(name, files);
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
				removeFilesAndEnsure();
				createAndPerformChange(refactoring);
				filesDoExist();
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

	protected ICProject createProject(final boolean cpp) throws CoreException {
		final ICProject cprojects[] = new ICProject[1];
		// Create the cproject
		final String projectName = "CodanProjTest_" + System.currentTimeMillis();
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.run(new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				// Create the cproject
				ICProject cproject = cpp ? CProjectHelper.createCCProject(projectName, null, IPDOMManager.ID_NO_INDEXER) : CProjectHelper.createCProject(projectName, null,
						IPDOMManager.ID_NO_INDEXER);
				cprojects[0] = cproject;
			}
		}, null);
		return cprojects[0];
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

	public void indexFiles() throws CoreException {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.run(new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				cproject.getProject().refreshLocal(1, monitor);
			}
		}, null);
		// Index the cproject
		CCorePlugin.getIndexManager().setIndexerId(cproject, IPDOMManager.ID_FAST_INDEXER);
		CCorePlugin.getIndexManager().reindex(cproject);
		// wait until the indexer is done
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(1000 * 60, // 1 min
				new NullProgressMonitor()));
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

	private void removeFilesAndEnsure() throws Exception {
		removeFiles();
		filesDoNotExist();
	}

	private void filesDoExist() {
		for (String fileName : newFiles) {
			if (!fileName.isEmpty()) {
				IFile file = project.getFile(new Path(fileName));
				assertTrue(file.exists());
			}
		}
	}

	private void filesDoNotExist() {
		for (String fileName : newFiles) {
			if (!fileName.isEmpty()) {
				IFile file = project.getFile(new Path(fileName));
				assertFalse(file.exists());
			}
		}
	}

	private void removeFiles() throws CoreException {
		for (String fileName : newFiles) {
			if (!fileName.isEmpty()) {
				IFile file = project.getFile(new Path(fileName));
				file.delete(true, NULL_PROGRESS_MONITOR);
			}
		}
	}
}
