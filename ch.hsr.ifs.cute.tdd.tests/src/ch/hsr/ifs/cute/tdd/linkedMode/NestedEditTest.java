/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.linkedMode;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.ifs.cute.tdd.LinkedMode.ChangeNotSupportedException;
import ch.hsr.ifs.cute.tdd.LinkedMode.NestedEdit;
import ch.hsr.ifs.cute.tdd.ui.tests.FakeChange;

public class NestedEditTest {

	private static final int OFFSET = 0;
	private static final String TEXT = "void testFoo(){foo();}";
	private static final String INSERTED_TEXT = "void foo(){}";
	private FakeChange fakeChange;
	private NestedEdit nested;

	@Before
	public void setUp() throws Exception {
		fakeChange = new FakeChange(TEXT, OFFSET, INSERTED_TEXT);
		nested = new NestedEdit(fakeChange);
	}

	@Test
	public void testEditHasSameText() {
		assertEquals(fakeChange.getInsertedText(), nested.getText());
	}

	@Test(
			expected = ChangeNotSupportedException.class)
	public void testIllegalChange() {
		new NestedEdit(new NullChange());
	}

	@Test(
			expected = ChangeNotSupportedException.class)
	public void testIllegalChangeChildren() {
		Change[] children = new Change[] { new CompositeChange("test", new Change[] {}) };
		new NestedEdit(new CompositeChange("test", children));
	}

	@Test
	public void testInsertEditChild() {
		String insertText = "new";
		NestedEdit edit = new NestedEdit(new FakeChange("", 0, insertText));
		assertEquals(insertText, edit.getText());
	}

	@Test
	public void testChangeHasEffect() throws CoreException {
		assertEquals(INSERTED_TEXT, nested.getText());
	}

	@Test
	public void testIndexOf() {
		String functionName = "foo";
		assertEquals(OFFSET + INSERTED_TEXT.indexOf(functionName), nested.absoluteIndexOf(functionName));
	}

	@Test
	public void testOffset() {
		assertEquals(OFFSET, nested.getOffset());
	}

	@Test
	public void testOverridenTextLength() {
		assertEquals(0, nested.getLength());
	}

	@Test
	public void testInsertedTextLength() {
		assertEquals(INSERTED_TEXT.length(), nested.getTextLength());
	}
}
