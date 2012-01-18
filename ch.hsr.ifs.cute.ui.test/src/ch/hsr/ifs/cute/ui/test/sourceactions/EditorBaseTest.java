/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.test.sourceactions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.testplugin.Accessor;
import org.eclipse.cdt.ui.testplugin.DisplayHelper;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.tests.BaseUITestCase;
import org.eclipse.cdt.ui.tests.refactoring.TestHelper;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDE;

import ch.hsr.ifs.cute.ui.test.UiTestPlugin;

/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 * 
 */
@SuppressWarnings("restriction")
public class EditorBaseTest extends BaseUITestCase {

	protected static final int INDEXER_WAIT_TIME = 8000;
	protected ICProject cProject;
	protected IIndex index;

	/**
	 * 
	 */
	public EditorBaseTest() {
	}

	/**
	 * @param name
	 */
	public EditorBaseTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		cProject = CProjectHelper.createCCProject("cuteTest", "bin", IPDOMManager.ID_FAST_INDEXER); //$NON-NLS-1$//$NON-NLS-2$
		CCorePlugin.getIndexManager().joinIndexer(INDEXER_WAIT_TIME, npm());
		index = CCorePlugin.getIndexManager().getIndex(cProject);
	}

	@Override
	protected void tearDown() throws Exception {

		closeAllEditors();
		index = null;
		if (cProject != null) {
			CProjectHelper.delete(cProject);
			cProject = null;
		}
		super.tearDown();
	}

	protected IProject getProject() {
		return cProject.getProject();
	}

	protected CEditor openEditor(IFile file) throws PartInitException {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor = (CEditor) IDE.openEditor(page, file);
		EditorTestHelper.joinReconciler(EditorTestHelper.getSourceViewer(editor), 100, 1000, 10);
		return editor;
	}

	@Override
	public StringBuffer[] getContentsForTest(int sections) throws IOException {
		return TestSourceReader.getContentsForTest(UiTestPlugin.getDefault().getBundle(), "src", getClass(), getName(), sections); //$NON-NLS-1$
	}

	protected void type(String text, int keyCode, int stateMask, CEditor editor) {
		for (char c : text.toCharArray()) {
			sendTypeEvent(c, keyCode, stateMask, editor);
		}
		waitForDisplay();

	}

	protected void type(char character, int keyCode, int stateMask, CEditor editor) {
		sendTypeEvent(character, keyCode, stateMask, editor);
		waitForDisplay();

	}

	protected void waitForDisplay() {
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return false;
			}
		}.waitForCondition(EditorTestHelper.getActiveDisplay(), 200);
	}

	protected void sendTypeEvent(char character, int keyCode, int stateMask, CEditor editor) {
		StyledText textWidget = editor.getViewer().getTextWidget();
		assertNotNull(textWidget);
		Accessor accessor = new Accessor(textWidget, StyledText.class);
		Event event = new Event();
		event.character = character;
		event.keyCode = keyCode;
		event.stateMask = stateMask;
		accessor.invoke("handleKeyDown", new Object[] { event }); //$NON-NLS-1$
	}

	protected String getCodeFromIFile(IFile file) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(file.getContents()));
		StringBuilder code = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			code.append(line);
			code.append('\n');
		}
		br.close();
		return code.toString();
	}

	protected void executeCommand(String commandId) throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
		IHandlerService hs = (IHandlerService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IHandlerService.class);
		hs.executeCommand(commandId, null);
	}

	protected IFile createFile(String fileContent, String fileName) throws Exception {
		IFile file = createFile(getProject(), fileName, fileContent);
		waitForIndexer(index, file, INDEXER_WAIT_TIME);
		return file;
	}

	protected void runCommand(IFile file, int selectionStart, int selechtionLength, String command) throws Exception {
		CEditor editor = openEditor(file);
		runCommand(selectionStart, selechtionLength, command, editor);
	}

	protected void runCommand(int selectionStart, int selechtionLength, String command, CEditor editor) throws ExecutionException, NotDefinedException, NotEnabledException,
			NotHandledException {
		editor.selectAndReveal(selectionStart, selechtionLength);
		executeCommand(command);
		saveEditor(editor);
	}

	protected void saveEditor(IEditorPart editor) {
		if (editor != null && editor instanceof TextEditor) {
			if (editor.isDirty()) {
				editor.doSave(new NullProgressMonitor());
			}
		}
	}

	protected void assertFileContent(IFile file, String expectedContent) throws Exception {
		String newContent = getCodeFromIFile(file);
		assertEquals(TestHelper.unifyNewLines(expectedContent), TestHelper.unifyNewLines(newContent));
	}
}
