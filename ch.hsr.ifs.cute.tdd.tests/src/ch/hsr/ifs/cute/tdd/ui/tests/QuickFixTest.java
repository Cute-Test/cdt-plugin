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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.ComparisonFailure;

import org.eclipse.cdt.codan.QuickFixTestCase;
import org.eclipse.cdt.codan.internal.core.model.CodanProblemMarker;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.ide.IDE;
import org.junit.Before;
import org.osgi.framework.Bundle;

import ch.hsr.ifs.cute.tdd.TddQuickFix;

@SuppressWarnings("restriction")
public abstract class QuickFixTest extends QuickFixTestCase {

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		System.setProperty("line.separator", "\n");
		enableAllProblems();
		loadcode(getCommentOfMethod("getCode"), true);
		indexFiles();
		runCodan();
		ensureMarkersReady();
	}

	public void ensureMarkersReady() throws CoreException {
		for (int i = 0; i < 10; i++) {
			if (markers == null || markers.length == 0) {
				indexFiles();
				dispatch(1000);
				runCodan();
			} else {
				break;
			}
		}
		if (markers == null || markers.length == 0) {
			fail("No markers found, but could also be an indexer problem.");
		}
	}

	public String getCommentOfTest() {
		return getCommentOfMethod(getName());
	}

	public String getCommentOfMethod(String name) {
		try {
			return TestSourceReader.getContentsForTest(getBundle(),	"src", getClass(), name, 1)[0].toString();
		} catch (IOException e) {
			fail("Could not load comment of test " + name);
			return "";
		}
	}

	private Bundle getBundle() {
		return Activator.getDefault().getBundle();
	}

	public String runQuickFix(Class<? extends TddQuickFix> klass, String qfmessage) {
		getQuickFix(klass, qfmessage).run(getFirstMarker());
		try {
			return loadFile(currentIFile.getContents());
		} catch (Exception e) {
			fail("Testframework failed loading file");
			return "";
		}
	}

	// Copied because TestUtils's loadFile method removed all newlines
	private static String loadFile(InputStream st) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(st));
		String buffer;
		StringBuffer result = new StringBuffer();
		while ((buffer = br.readLine()) != null) {
			result.append(buffer + '\n');
		}
		st.close();
		return result.toString();
	}

	public IMarker getFirstMarker() {
		for (IMarker m: markers) {
			if (hasRightType(m)) {
				return m;
			}
		}
		fail("No marker of type " + getId() + " found");
		return null;
	}

	private boolean hasRightType(IMarker m) {
		return getType(m).equals("org.eclipse.cdt.codan.core.codanProblem") &&
				CodanProblemMarker.getProblemId(m).equals(getId());
	}

	private String getType(IMarker m) {
		try {
			return m.getType();
		} catch (CoreException e) {
		}
		return "";
	}

	public int getMarkerLength() {
		return getFirstMarker().getAttribute(IMarker.CHAR_END, 0) - getFirstMarker().getAttribute(IMarker.CHAR_START, 0);
	}

	public int getMarkerOffset() {
		return getFirstMarker().getAttribute(IMarker.CHAR_START, -1);
	}

	public String getMarkerMessage() {
		if (markers.length < 1) {
			fail("No markers found");
		}
		return getFirstMarker().getAttribute(IMarker.MESSAGE, "");
	}

	public String getQuickFixMessage(Class<? extends TddQuickFix> klass, String qfmessage) {
		TddQuickFix quickFix = getQuickFix(klass, qfmessage);
		quickFix.isApplicable(getFirstMarker());
		return quickFix.getLabel();
	}

	public TddQuickFix getQuickFix(Class<? extends TddQuickFix> klass, String qfmessage) {
		return getQuickFix(getFirstMarker(), klass, qfmessage);
	}

	@Override
	protected TddQuickFix createQuickFix() {
		return null;
	}

	public void assertExactlytheSame(String expected, String result) {
		if (!result.equals(expected)) {
			throw new ComparisonFailure("Text <" + expected + "> not found in <" + result + ">", expected, result);
		}
	}

	public TddQuickFix getQuickFix(IMarker marker, Class<? extends TddQuickFix> klass, String qfmessage) {
		IMarkerResolution[] resolutions = IDE.getMarkerHelpRegistry().getResolutions(marker);
		assertTrue(resolutions.length > 0);
		for(IMarkerResolution resolution: resolutions) {
			if (klass == resolution.getClass()) {
				TddQuickFix tddqf = (TddQuickFix)resolution;
				tddqf.isApplicable(marker);
				if (tddqf.getLabel().equals(qfmessage)) {
					return (TddQuickFix) resolution;
				} else {
					System.err.println("Type of resolution matches, but message does not! Quickfix: " + tddqf.getLabel() + " message: " + qfmessage+ " Possible Typo?");
				}
			}
		}
		fail("Could not find a resolution that matches");
		return null;
	}

	protected abstract String getId();
	protected abstract void getCode();
	public abstract void testMarkerMessage();
	public abstract void testMarkerOffset();
	public abstract void testMarkerLength();
	public abstract void testQuickFixMessage();
	public abstract void testQuickFixApplying();
	public abstract void testImageNotNull();
}
