/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.tests.sourceactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.eclipse.cdt.ui.ICEditor;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.junit.Test;

import ch.hsr.ifs.cute.ui.commands.handlers.NewTestFunction;
import ch.hsr.ifs.iltis.testing.highlevel.testingplugin.cdttest.base.CDTTestingUITest;


public class TestBugFixes extends CDTTestingUITest {

    @Test
    public void testNewTestFunctionhighlight() throws Exception {
        final IEditorPart editor = EditorTestHelper.openInEditor(getPrimaryIFileFromCurrentProject(), true);
        assertTrue(editor instanceof ICEditor);

        final ISelectionProvider selectionProvider = ((ICEditor) editor).getSelectionProvider();
        final NewTestFunction commandHandler = new NewTestFunction();
        commandHandler.execute(null);

        LinkedModeUI linkedModeUI = getLinkedModeUI(commandHandler);
        callLeave(linkedModeUI);

        final String results = currentProjectHolder.getDocument(getPrimaryIFileFromCurrentProject()).get();

        final TextSelection selection = (TextSelection) selectionProvider.getSelection();
        final String actual = results.substring(selection.getOffset(), selection.getOffset() + selection.getLength());

        assertEquals("ASSERTM(\"start writing tests\", false);", actual);
    }

    private static LinkedModeUI getLinkedModeUI(NewTestFunction handler) {
        // @formatter:off
        return Arrays.stream(NewTestFunction.class.getDeclaredFields())
                .filter(f -> f.getName().equals("linkedModeUI"))
                .findFirst()
                .map(f -> {
                    f.setAccessible(true);
                    try {
                        return (LinkedModeUI) f.get(handler);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        return null;
                    }
        }).get();
        // @formatter:on
    }

    private void callLeave(final LinkedModeUI linked2ndCopy) throws IllegalAccessException, InvocationTargetException {
        // default access, so need reflection
        boolean flag = true;
        final java.lang.reflect.Method[] methods = LinkedModeUI.class.getDeclaredMethods();
        for (final Method method : methods) {
            if (method.getName().equals("leave")) {
                final Object params[] = { ILinkedModeListener.UPDATE_CARET };
                method.setAccessible(true);
                method.invoke(linked2ndCopy, params);
                flag = false;
            }
        }
        assertFalse(flag);
    }
}
