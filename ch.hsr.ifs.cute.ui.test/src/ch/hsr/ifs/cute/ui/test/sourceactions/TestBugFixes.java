/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.test.sourceactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.junit.After;
import org.junit.Test;

import ch.hsr.ifs.cute.ui.sourceactions.NewTestFunctionActionDelegate;
import ch.hsr.ifs.cute.ui.test.fakebasetests.BaseTestFramework;

@SuppressWarnings("restriction")
public class TestBugFixes extends BaseTestFramework {

	private static final String TEST_DEFS = "testDefs/cute/sourceActions/bugfix.cpp";
	protected static CEditor ceditor;

	@Test
	public void testNewTestFunctionhighlight() throws Exception {
		ReadTestCase rtc = new ReadTestCase(TEST_DEFS, false);

		Integer[] cursorpos = { 212, 261 };
		rtc.removeCaretFromTest();
		String testSrcCode = rtc.test.get(0);

		IFile inputFile = importFile("A.cpp", testSrcCode);
		IEditorPart editor = EditorTestHelper.openInEditor(inputFile, true);
		assertNotNull(editor);
		assertTrue(editor instanceof CEditor);
		ceditor = (CEditor) editor;
		ISelectionProvider selectionProvider = ceditor.getSelectionProvider();

		selectionProvider.setSelection(new TextSelection(cursorpos[0], 0));

		NewTestFunctionActionDelegate ntfad = new NewTestFunctionActionDelegate();
		ntfad.run(null);

		ntfad.testOnlyGetLinkedMode();

		// set cursor location to be at the newly created newTest^Function
		selectionProvider.setSelection(new TextSelection(cursorpos[1], 0));
		ntfad.run(null);

		LinkedModeUI linked2ndCopy = ntfad.testOnlyGetLinkedMode();
		linked2ndCopy.getSelectedRegion();

		// default access, so need reflection
		boolean flag = true;
		final java.lang.reflect.Method[] methods = LinkedModeUI.class.getDeclaredMethods();
		for (int i = 0; i < methods.length; ++i) {
			if (methods[i].getName().equals("leave")) {
				final Object params[] = { ILinkedModeListener.UPDATE_CARET };
				methods[i].setAccessible(true);
				methods[i].invoke(linked2ndCopy, params);
				flag = false;
			}
		}
		assertFalse(flag);

		String results = getText(editor);

		ISelection see = selectionProvider.getSelection();
		TextSelection selection = (TextSelection) see;

		results = getText(editor);
		results = results.substring(selection.getOffset(), selection.getOffset() + selection.getLength());

		String expected = "ASSERTM(\"start writing tests\", false);";
		assertEquals(expected, results);

	}

	private String getText(IEditorPart editor) {
		Object ele = (editor).getEditorInput();
		IDocumentProvider idp = ceditor.getDocumentProvider();
		IDocument fDocument = idp.getDocument(ele);
		return fDocument.get();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		EditorTestHelper.closeEditor(ceditor);
		super.tearDown();
	}

}
