/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.test.sourceactions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.ui.ICEditor;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingTest;
import ch.hsr.ifs.cute.ui.sourceactions.NewTestFunctionActionDelegate;

public class TestBugFixes extends CDTTestingTest {

	@Test
	public void testNewTestFunctionhighlight() throws Exception {
		IEditorPart editor = EditorTestHelper.openInEditor(getActiveIFile(), true);
		assertTrue(editor instanceof ICEditor);

		ISelectionProvider selectionProvider = ((ICEditor) editor).getSelectionProvider();
		selectionProvider.setSelection(new TextSelection(212, 0));

		NewTestFunctionActionDelegate ntfad = new NewTestFunctionActionDelegate();
		ntfad.run(null);

		// set cursor location to be at the newly created newTest^Function
		selectionProvider.setSelection(new TextSelection(261, 0));
		ntfad.run(null);

		LinkedModeUI linked2ndCopy = ntfad.testOnlyGetLinkedMode();
		linked2ndCopy.getSelectedRegion();

		callLeave(linked2ndCopy);

		String results = getCurrentSource();

		TextSelection selection = (TextSelection) selectionProvider.getSelection();
		String actual = results.substring(selection.getOffset(), selection.getOffset() + selection.getLength());

		assertEquals("ASSERTM(\"start writing tests\", false);", actual);
	}

	private void callLeave(LinkedModeUI linked2ndCopy) throws IllegalAccessException, InvocationTargetException {
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
	}
}
