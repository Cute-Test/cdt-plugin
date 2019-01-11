/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.tests.parser;

import java.util.List;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.cute.gcov.GcovPlugin;
import ch.hsr.ifs.cute.gcov.model.CoverageModel;
import ch.hsr.ifs.cute.gcov.model.CoverageStatus;
import ch.hsr.ifs.cute.gcov.model.File;
import ch.hsr.ifs.cute.gcov.model.Function;
import ch.hsr.ifs.cute.gcov.model.Line;
import ch.hsr.ifs.cute.gcov.parser.GcovFileParser;
import ch.hsr.ifs.cute.gcov.tests.mock.MockFile;
import junit.framework.TestCase;


/**
 * @author Emanuel Graf IFS
 *
 */
public class ModelBuilderLineParserTest extends TestCase {

    private static final String TEST_FILE_NAME = "testFile.cpp";
    private static final String GCOV_FILE_NAME = TEST_FILE_NAME + ".gcov";
    private static final String PROJECT_NAME   = "project";
    private MockFile            gcovFile;
    private ICProject           cProject;
    private IFile               testFile;

    @Override
    protected void setUp() throws Exception {
        cProject = CProjectHelper.createCCProject(PROJECT_NAME, "bin");
        testFile = TestSourceReader.createFile(cProject.getProject(), TEST_FILE_NAME, "//only a comment");
    }

    @Override
    protected void tearDown() throws Exception {
        GcovPlugin.getDefault().getcModel().clearModel();
        if (cProject != null) {
            CProjectHelper.delete(cProject);
        }
        testFile = null;
        gcovFile = null;
        super.tearDown();
    }

    public void testParseFile() {
        String content = "\n";
        gcovFile = new MockFile(cProject.getPath().append(GCOV_FILE_NAME), content);
        CoverageModel model = runParser();
        File modelFile = model.getModelForFile(gcovFile);
        assertNull(modelFile);
    }

    public void testUncoveredEmptyFunction() {
        String content =
                       "0:Source:testFile.cpp\nfunction _Z11emptyMethodv called 0 returned 0% blocks executed 0%\n    #####:   12:/*EOF*/\n    #####:   13:/*EOF*/\n        -:   14:/*EOF*/";
        gcovFile = new MockFile(cProject.getPath().append(GCOV_FILE_NAME), content);
        String markerType = GcovPlugin.UNCOVER_MARKER_TYPE;
        emptyMethod(markerType, CoverageStatus.Uncovered, 12, 2);
    }

    public void testCoveredEmptyFunction() {
        String content =
                       "0:Source:testFile.cpp\nfunction _Z11emptyMethodv called 0 returned 100% blocks executed 100%\n    1:   12:/*EOF*/\n    1:   13:/*EOF*/\n        -:   14:/*EOF*/";
        gcovFile = new MockFile(cProject.getPath().append(TEST_FILE_NAME), content);
        String markerType = GcovPlugin.COVER_MARKER_TYPE;
        emptyMethod(markerType, CoverageStatus.Covered, 12, 2);
    }

    public void testPartiallyCoveredFunction() {
        String content =
                       "0:Source:testFile.cpp\nfunction _ZN5Hallo6foobarEi called 3 returned 100% blocks executed 75%\n        3:   20:/*EOF*/\nbranch  0 taken 0% (fallthrough)\nbranch  1 taken 100%";
        gcovFile = new MockFile(cProject.getPath().append(TEST_FILE_NAME), content);
        String markerType = GcovPlugin.PARTIALLY_MARKER_TYPE;
        emptyMethod(markerType, CoverageStatus.PartiallyCovered, 20, 1);
    }

    protected void emptyMethod(String markerType, CoverageStatus coverageStatus, int lineNumber, int lineCount) {
        CoverageModel model = runParser();
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
        for (IMarker marker : gcovFile.getMarkers()) {
            try {
                assertEquals(markerType, marker.getType());
                assertEquals(lineNr, marker.getAttribute(IMarker.LINE_NUMBER, -1));
                ++lineNr;
            } catch (CoreException e) {
                fail("Exception: " + e.getLocalizedMessage());
            }
        }
    }

    protected CoverageModel runParser() {
        try {
            GcovFileParser parser = new GcovFileParser(gcovFile, cProject.getProject().getLocation());
            parser.parse();
        } catch (CoreException e) {
            fail("Exception: " + e.getLocalizedMessage());
        }
        CoverageModel model = GcovPlugin.getDefault().getcModel();
        return model;
    }
}
