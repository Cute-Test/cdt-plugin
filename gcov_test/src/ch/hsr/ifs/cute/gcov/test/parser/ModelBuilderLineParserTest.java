/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.test.parser;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import ch.hsr.ifs.cute.gcov.GcovPlugin;
import ch.hsr.ifs.cute.gcov.model.CoverageModel;
import ch.hsr.ifs.cute.gcov.model.CoverageStatus;
import ch.hsr.ifs.cute.gcov.model.File;
import ch.hsr.ifs.cute.gcov.model.Function;
import ch.hsr.ifs.cute.gcov.model.Line;
import ch.hsr.ifs.cute.gcov.parser.ModelBuilderLineParser;
import ch.hsr.ifs.cute.gcov.test.mock.MockFile;

/**
 * @author Emanuel Graf IFS
 * 
 */
public class ModelBuilderLineParserTest extends TestCase {

	private static final String TEST_FILE_NAME = "testFile.cpp"; //$NON-NLS-1$
	private static final String PROJECT_NAME = "project"; //$NON-NLS-1$
	private ModelBuilderLineParser parser;
	private MockFile testFile;

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	protected void setUp() throws Exception {
		parser = new ModelBuilderLineParser();
		testFile = new MockFile(new Path(PROJECT_NAME + "/" + TEST_FILE_NAME)); //$NON-NLS-1$
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	protected void tearDown() throws Exception {
		GcovPlugin.getDefault().getcModel().clearModel();
	}

	/**
	 * Test method for {@link ch.hsr.ifs.cute.gcov.parser.ModelBuilderLineParser#parse(org.eclipse.core.resources.IFile, java.io.Reader)}.
	 */
	public void testParseFile() {
		Reader reader = createReader(""); //$NON-NLS-1$
		CoverageModel model = runParser(reader);
		File modelFile = model.getModelForFile(testFile);
		assertNotNull(modelFile);
		assertEquals(TEST_FILE_NAME, modelFile.getFileName());
	}

	public void testUncoveredEmptyFunction() {
		Reader reader = createReader("function _Z11emptyMethodv called 0 returned 0% blocks executed 0%\n    #####:   12:/*EOF*/\n    #####:   13:/*EOF*/\n        -:   14:/*EOF*/"); //$NON-NLS-1$
		String markerType = GcovPlugin.UNCOVER_MARKER_TYPE;
		emptyMethod(reader, markerType, CoverageStatus.Uncovered, 12, 2);
	}

	public void testCoveredEmptyFunction() {
		Reader reader = createReader("function _Z11emptyMethodv called 0 returned 100% blocks executed 100%\n    1:   12:/*EOF*/\n    1:   13:/*EOF*/\n        -:   14:/*EOF*/"); //$NON-NLS-1$
		String markerType = GcovPlugin.COVER_MARKER_TYPE;
		emptyMethod(reader, markerType, CoverageStatus.Covered, 12, 2);
	}

	public void testPartiallyCoveredFunction() {
		Reader reader = createReader("function _ZN5Hallo6foobarEi called 3 returned 100% blocks executed 75%\n        3:   20:/*EOF*/\nbranch  0 taken 0% (fallthrough)\nbranch  1 taken 100%"); //$NON-NLS-1$
		String markerType = GcovPlugin.PARTIALLY_MARKER_TYPE;
		emptyMethod(reader, markerType, CoverageStatus.PartiallyCovered, 20, 1);
	}

	protected void emptyMethod(Reader reader, String markerType, CoverageStatus coverageStatus, int lineNumber, int lineCount) {
		CoverageModel model = runParser(reader);
		File modelFile = model.getModelForFile(testFile);
		List<Function> functions = modelFile.getFunctions();
		assertEquals(1, functions.size());
		Function f = functions.get(0);
		List<Line> lines = f.getLines();
		assertEquals(lineCount, lines.size());
		for (Line line : lines) {
			assertEquals(coverageStatus, line.getStatus());
		}
		int lineNr = lineNumber;
		for (IMarker marker : testFile.getMarkers()) {
			try {
				assertEquals(markerType, marker.getType());
				assertEquals(lineNr, marker.getAttribute(IMarker.LINE_NUMBER, -1));
				++lineNr;
			} catch (CoreException e) {
				fail("Exception: " + e.getLocalizedMessage()); //$NON-NLS-1$
			}
		}
	}

	protected CoverageModel runParser(Reader reader) {
		try {
			parser.parse(testFile, reader);
		} catch (CoreException e) {
			fail("Exception: " + e.getLocalizedMessage()); //$NON-NLS-1$
		}
		CoverageModel model = GcovPlugin.getDefault().getcModel();
		return model;
	}

	private Reader createReader(String string) {
		return new StringReader(string);
	}

}
