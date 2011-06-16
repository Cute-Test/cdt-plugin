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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.ifs.cute.tdd.LinkedMode.ChangeRecorder;
import ch.hsr.ifs.cute.tdd.ui.tests.FakeChange;

public class ChangeRecorderTest {

	private FakeChange change;
	private ChangeRecorder recorder;

	private final String NAME = "foo";
	private final int MARKER_OFFSET_FUNC = 15;
	private final String INSTEXT_FUNC = "int foo(){return int(3);}";
	private Document OLDTEXT_FUNC;
	private Document NEWTEXT_FUNC;

	@Before
	public void setUp() {
		OLDTEXT_FUNC = new Document("void testFoo(){int x = foo();}");
		NEWTEXT_FUNC = new Document(INSTEXT_FUNC + OLDTEXT_FUNC.get());
		change = new FakeChange(OLDTEXT_FUNC, 0, INSTEXT_FUNC);
		recorder = new ChangeRecorder(MARKER_OFFSET_FUNC, OLDTEXT_FUNC, change , NAME);
	}

	@Test
	public void testExecutePerformsChange() {
		assertEquals(NEWTEXT_FUNC.get(), recorder.getDocument().get());
	}

	@Test(expected=RuntimeException.class)
	public void testExecuteFailingChange() {
		change.setFailing();
		recorder = new ChangeRecorder(MARKER_OFFSET_FUNC, OLDTEXT_FUNC, change , NAME);
	}

	@Test
	public void testSpecEnd() throws BadLocationException {
		assertEquals(3, recorder.getSpecEnd());
	}

	@Test
	public void testSpecLength() throws BadLocationException {
		assertEquals(3, recorder.getSpecLength());
	}

	@Test
	public void testSpecBegin() throws BadLocationException {
		assertEquals(0, recorder.getSpecBegin());
	}

	@Test
	public void testBracketPosition() throws BadLocationException {
		assertEquals(10, recorder.getBracketPosition());
	}

	@Test
	public void testEndOfMarkedLine() throws BadLocationException {
		assertEquals(54, recorder.getEndOfMarkedLine());
	}

	@Test
	public void testMarkerLineEndWhenSemicolonWasMissing() throws BadLocationException {
		OLDTEXT_FUNC = new Document("void testFoo(){int x = foo()\n}");
		change = new FakeChange(OLDTEXT_FUNC, 0, "int foo(){return int(3);}");
		recorder = new ChangeRecorder(MARKER_OFFSET_FUNC, OLDTEXT_FUNC, change , NAME);
		assertEquals(53, recorder.getEndOfMarkedLine());
	}

	@Test(expected=RuntimeException.class)
	public void testMarkerLineEndWhenEverythingIsMissing() throws BadLocationException {
		OLDTEXT_FUNC = new Document("void testFoo(){int x = foo()}");
		change = new FakeChange(OLDTEXT_FUNC, 0, "int foo(){return int(3);}");
		recorder = new ChangeRecorder(MARKER_OFFSET_FUNC, OLDTEXT_FUNC, change , NAME);
		recorder.getEndOfMarkedLine();
	}

	@Test
	public void testSpecifierDetection() throws BadLocationException {
		assertEquals("int", recorder.getSpecifier());
	}

	@Test
	public void testReturnStatementOffset() {
		assertEquals(17, recorder.getRetBegin());
	}

	@Test
	public void testReturnValueLength() {
		assertEquals(6, recorder.getRetLength());
	}
	
	@Test
	public void testSingleParameterNameOffset() throws BadLocationException {
		setupFunctionWithParameters("42", "int const & i");
		assertEquals(20, recorder.getParameterPositions().get(0).getOffset());
	}
	
	@Test
	public void testSingleParameterNameLength() throws BadLocationException {
		setupFunctionWithParameters("42", "int const & i");
		assertEquals(1, recorder.getParameterPositions().get(0).getLength());
	}
	
	@Test
	public void testSingleParameterSpecOffset() throws BadLocationException {
		setupFunctionWithParameters("42", "int const & i");
		assertEquals(8, recorder.getParameterPositions().get(1).getOffset());
	}
	
	@Test
	public void testSingleParameterSpecLength() throws BadLocationException {
		setupFunctionWithParameters("42", "int const & i");
		assertEquals(11, recorder.getParameterPositions().get(1).getLength());
	}
	
	@Test
	public void testSecondParameterNameOffset() throws BadLocationException {
		setupFunctionWithParameters("42, 42", "int const & i, int const & j");
		assertEquals(35, recorder.getParameterPositions().get(1).getOffset());
	}
	
	@Test
	public void testSecondParameterNameLength() throws BadLocationException {
		setupFunctionWithParameters("42, 42", "int const & i, int const & j");
		assertEquals(1, recorder.getParameterPositions().get(1).getLength());
	}

	@Test
	public void testSecondParameterSpecOffset() throws BadLocationException {
		setupFunctionWithParameters("42, 42", "int const & i, int const & j");
		assertEquals(23, recorder.getParameterPositions().get(3).getOffset());
	}

	@Test
	public void testSecondParameterSpecLength() throws BadLocationException {
		setupFunctionWithParameters("42, 42", "int const & i, int const & j");
		assertEquals(11, recorder.getParameterPositions().get(3).getLength());
	}

	@Test
	public void testMethodConstOffset() throws BadLocationException {
		setupMethod("", "");
		assertEquals(19, recorder.getConstOffset());
	}

	private void setupFunctionWithParameters(String args, String params) {
		OLDTEXT_FUNC = new Document("void testFoo(){int x = foo(" + args + ")}");
		change = new FakeChange(OLDTEXT_FUNC, 0, "int foo(" + params + "){return int();}");
		recorder = new ChangeRecorder(MARKER_OFFSET_FUNC, OLDTEXT_FUNC, change , NAME);
	}

	private void setupMethod(String args, String params) {
		OLDTEXT_FUNC = new Document("struct A{};void testFoo(){A a;int x = a.foo(" + args + ")}");
		change = new FakeChange(OLDTEXT_FUNC, 9, "int foo(" + params + ") const {return int();}");
		recorder = new ChangeRecorder(MARKER_OFFSET_FUNC, OLDTEXT_FUNC, change , NAME);
	}
}
